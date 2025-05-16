package com.project.makecake.dto.store;

import com.project.makecake.domain.store.OpenTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenTimeResponseDto {

    private String type;
    private String startTime;
    private String endTime;
    private String descriptionTime;

    public OpenTimeResponseDto(OpenTime openTime){
        this.type = openTime.getType();
        this.startTime = openTime.getStartTime();
        this.endTime = openTime.getEndTime();
        this.descriptionTime = openTime.getDescriptionTime();
    }

    public OpenTimeResponseDto(String descriptionTime){
        this.type = "";
        this.startTime = "";
        this.endTime = "";
        this.descriptionTime = descriptionTime;
    }

}
