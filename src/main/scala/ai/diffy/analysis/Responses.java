package ai.diffy.analysis;

import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "Responses")
public class Responses {
	
	@Id
    @Column(name = "id")
    public String id;
	
	@Column(name = "prime", length = 40000)
    public String primary;
	
	@Column(name = "sec", length = 40000)
    public String secondary;
	
	@Column(name = "cand", length = 40000)
    public String candidate;

    public Responses(String id, String primary, String secondary, String candidate) {
        this.id = id;
        this.primary = primary;
        this.secondary = secondary;
        this.candidate = candidate; 
    }
}
