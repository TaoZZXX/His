package com.his.domain;

import java.math.BigDecimal;

public class DmsDrug {
    private Long id; private String code; private String name; private String format; private BigDecimal price; private String unit; private String manufacturer;
    private Long dosageId; private Long typeId; private String mnemonicCode; private Long stock; private String genericName; private Integer status;
    public Long getId(){return id;} public void setId(Long id){this.id=id;} public String getCode(){return code;} public void setCode(String code){this.code=code;}
    public String getName(){return name;} public void setName(String name){this.name=name;} public String getFormat(){return format;} public void setFormat(String format){this.format=format;}
    public BigDecimal getPrice(){return price;} public void setPrice(BigDecimal price){this.price=price;} public String getUnit(){return unit;} public void setUnit(String unit){this.unit=unit;}
    public String getManufacturer(){return manufacturer;} public void setManufacturer(String manufacturer){this.manufacturer=manufacturer;} public Long getDosageId(){return dosageId;} public void setDosageId(Long dosageId){this.dosageId=dosageId;}
    public Long getTypeId(){return typeId;} public void setTypeId(Long typeId){this.typeId=typeId;} public String getMnemonicCode(){return mnemonicCode;} public void setMnemonicCode(String mnemonicCode){this.mnemonicCode=mnemonicCode;}
    public Long getStock(){return stock;} public void setStock(Long stock){this.stock=stock;} public String getGenericName(){return genericName;} public void setGenericName(String genericName){this.genericName=genericName;}
    public Integer getStatus(){return status;} public void setStatus(Integer status){this.status=status;}
}
