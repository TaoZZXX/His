package com.his.domain;

public class SmsDept {
    private Long id;
    private String code;
    private Integer catId;
    private String name;
    private Integer type;
    private Integer status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Integer getCatId() { return catId; }
    public void setCatId(Integer catId) { this.catId = catId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
