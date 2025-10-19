package com.secondhand.common.doc.entity;



import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Param {

    private String name;

    private String value;

    private boolean required;

    private String dataType;

    private String note;

}
