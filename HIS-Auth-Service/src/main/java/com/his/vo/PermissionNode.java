package com.his.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PermissionNode {
    private Long id;
    private Long pid;
    private String name;
    private String value;
    private String url;
    private Integer status;
    private LocalDateTime createTime;
    private Integer sort;
    private Integer type;
    private List<PermissionNode> children = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPid() { return pid; }
    public void setPid(Long pid) { this.pid = pid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public List<PermissionNode> getChildren() { return children; }
    public void setChildren(List<PermissionNode> children) { this.children = children; }
}
