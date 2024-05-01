package ai.diffy.analysis

import ai.diffy.compare.Difference
import ai.diffy.compare.NoDifference
import ai.diffy.compare.PrimitiveDifference
import ai.diffy.flat.FlatEntry
import ai.diffy.flat.FlatObject
import ai.diffy.lifter.AnalysisRequest
import ai.diffy.lifter.JsonLifter
import ai.diffy.lifter.Message
import ai.diffy.repository.DifferenceResultRepository
import io.opentelemetry.api.trace.Span
import org.slf4j.LoggerFactory

import java.util.Date
import scala.jdk.CollectionConverters.SeqHasAsJava
import scala.language.postfixOps
import scala.util.Random

object DifferenceAnalyzer {
  val log = LoggerFactory.getLogger(classOf[DifferenceAnalyzer])
  val UndefinedEndpoint = Some("undefined_endpoint")
  def normalizeEndpointName(name: String) = name.replace("/", "-")
}

class DifferenceAnalyzer(
    rawCounter: RawDifferenceCounter,
    noiseCounter: NoiseDifferenceCounter,
    store: InMemoryDifferenceCollector)
{
  import DifferenceAnalyzer._

  def analyze(analysisRequest: AnalysisRequest): Option[DifferenceResult] = {
    apply(analysisRequest.request, analysisRequest.candidate, analysisRequest.primary, analysisRequest.secondary)
  }
  def apply(
    request: Message,
    candidate: Message,
    primary: Message,
    secondary: Message,
    idKnown: Option[String] = None
  ): Option[DifferenceResult] = {
    getEndpointName(request.endpoint, candidate.endpoint,
        primary.endpoint, secondary.endpoint) flatMap { endpointName =>
      val requestDiff: Map[String, Difference] =
        FlatObject.lift(request.result)
          .rendered map { case FlatEntry(key, value) =>
          s"request.$key.NoDifference" -> NoDifference(value)
        } toMap
//      val requestDiff =
//        Difference(request.result, request.result)
//          .flattened map {case (k,v) => s"request.$k" -> v}

      val rawDiff: Map[String, Difference] = requestDiff ++
        (Difference(primary.result, candidate.result)
          .flattened map {case (k,v) => s"response.$k" -> v})

      val noiseDiff = requestDiff ++
        (Difference(primary.result, secondary.result)
          .flattened map {case (k,v) => s"response.$k" -> v})

      val id = idKnown.getOrElse(new String(Random.alphanumeric.take(10).toArray))
      rawCounter.counter.count(endpointName, rawDiff)
      noiseCounter.counter.count(endpointName, noiseDiff ++ requestDiff)

      if (rawDiff.size > 0) {
        val diffResult = new DifferenceResult(
          id,
          Span.current().getSpanContext.getTraceId,
          endpointName,
          new Date().getTime,
          differencesToJson(rawDiff).asJava,
          JsonLifter.encode(request.result),
          new Responses(
            id,
            JsonLifter.encode(primary.result),
            JsonLifter.encode(secondary.result),
            JsonLifter.encode(candidate.result)
          ));
        store.create(diffResult)
        Some(diffResult)
      } else {
        log.debug(s"endpoint[$endpointName]diff[$id]=NoDifference")
        None
      }
    }
  }

  def clear(): Unit = {
    rawCounter.counter.clear()
    noiseCounter.counter.clear()
    store.clear()
  }

  def differencesToJson(diffs: Map[String, Difference]): Seq[FieldDifference] =
    diffs.toSeq map {
      case (field, diff @ PrimitiveDifference(_: Long, _)) =>
        new FieldDifference(
          field,
          JsonLifter.encode(
            diff.toMap map {
              case (k, v) => k -> v.toString
            }
          )
        )

      case (field, diff) => new FieldDifference(field, JsonLifter.encode(diff.toMap))
    }

  private[this] def getEndpointName(
      requestEndpoint: Option[String],
      candidateEndpoint: Option[String],
      primaryEndpoint: Option[String],
      secondaryEndpoint: Option[String]): Option[String] = {
    val rawEndpointName = (requestEndpoint, candidateEndpoint, primaryEndpoint, secondaryEndpoint) match {
      case (Some(_), _, _, _) => requestEndpoint
      // undefined endpoint when action header is missing from all three instances
      case (_, None, None, None) => UndefinedEndpoint
      // the assumption is that primary and secondary should call the same endpoint,
      // otherwise it's noise and we should discard the request
      case (_, None, _, _) if primaryEndpoint == secondaryEndpoint => primaryEndpoint
      case (_, None, _, _) => None
      case (_, Some(_), _, _) => candidateEndpoint
    }

    rawEndpointName map { normalizeEndpointName(_) }
  }
}
