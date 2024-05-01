package ai.diffy.analysis;

import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "Field_Difference")
public class FieldDifference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@Column(name = "field", length = 40000)
    public String field;
	
	@Column(name = "difference" , length = 40000)
    public String difference;

    public FieldDifference(String field, String difference) {
        this.field = field;
        this.difference = difference;
    }
}
