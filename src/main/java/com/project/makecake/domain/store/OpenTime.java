package com.project.makecake.domain.store;

import com.project.makecake.domain.Timestamped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class OpenTime extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long openTimeId;

    @Column
    private String type;

    @Column
    private String startTime;

    @Column
    private String endTime;

    @Column
    private String descriptionTime;

    @Column
    private Boolean isDayOff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STORE_ID")
    private Store store;

}
