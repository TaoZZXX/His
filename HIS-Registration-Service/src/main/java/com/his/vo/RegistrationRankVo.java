package com.his.vo;

import java.math.BigDecimal;

public class RegistrationRankVo {
    private Long id;
    private String code;
    private String name;
    private Long seqNo;
    private BigDecimal price;
    private Integer status;
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getCode(){return code;} public void setCode(String code){this.code=code;}
    public String getName(){return name;} public void setName(String name){this.name=name;}
    public Long getSeqNo(){return seqNo;} public void setSeqNo(Long seqNo){this.seqNo=seqNo;}
    public BigDecimal getPrice(){return price;} public void setPrice(BigDecimal price){this.price=price;}
    public Integer getStatus(){return status;} public void setStatus(Integer status){this.status=status;}
}
