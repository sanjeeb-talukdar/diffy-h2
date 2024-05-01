package ai.diffy.repository;

import java.util.List;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;


@Getter
@Setter
@Entity
@NoArgsConstructor
@Table
public class Noise {
	
    @Id 
    @Column(name = "endpoint", length = 40000)
    public String endpoint;
  
    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "noisyfields", joinColumns = @JoinColumn(name = "endpoint"))
    @Column(name = "noisyfields", nullable = false , length = 40000)
    public List<String> noisyfields;

    public Noise(String endpoint, List<String> noisyfields) {
        this.endpoint = endpoint;
        this.noisyfields = noisyfields;
    }
}
