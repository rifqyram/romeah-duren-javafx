package ac.unindra.roemah_duren_spring.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OutcomeRequest {
    private String id;
    private Long amount;
    private String description;
    private String branchId;
}
