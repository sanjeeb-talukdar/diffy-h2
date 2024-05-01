package ai.diffy.transformations;


import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "Transformation")
public class Transformation {
    
	@Id
    @Column(name = "injection_Point", length = 40000)
    public String injectionPoint;
	
    @Column(name = "transformation_Js", length = 40000)
    public String transformationJs;

    public Transformation(String injectionPoint, String transformationJs) {
        this.injectionPoint = injectionPoint;
        this.transformationJs = transformationJs;
    }

    public String getTransformationJs() {
        return transformationJs;
    }
}
