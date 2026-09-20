package com.his.domain;

import java.math.BigDecimal;

public class DmsNonDrug {
    private Long id; private String code; private String name; private String format; private BigDecimal price; private Long expClassId; private String mnemonicCode; private Integer recordType; private Integer status; private Long deptId;
    public Long getId(){return id;} public void setId(Long id){this.id=id;} public String getCode(){return code;} public void setCode(String code){this.code=code;}
    public String getName(){return name;} public void setName(String name){this.name=name;} public String getFormat(){return format;} public void setFormat(String format){this.format=format;}
    public BigDecimal getPrice(){return price;} public void setPrice(BigDecimal price){this.price=price;} public Long getExpClassId(){return expClassId;} public void setExpClassId(Long expClassId){this.expClassId=expClassId;}
    public String getMnemonicCode(){return mnemonicCode;} public void setMnemonicCode(String mnemonicCode){this.mnemonicCode=mnemonicCode;} public Integer getRecordType(){return recordType;} public void setRecordType(Integer recordType){this.recordType=recordType;}
    public Integer getStatus(){return status;} public void setStatus(Integer status){this.status=status;} public Long getDeptId(){return deptId;} public void setDeptId(Long deptId){this.deptId=deptId;}
}
