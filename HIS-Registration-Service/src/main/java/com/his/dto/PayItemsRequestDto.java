package com.his.dto;

import java.util.List;

public class PayItemsRequestDto {
    private List<Long> payableItemIds;
    public List<Long> getPayableItemIds(){return payableItemIds;}
    public void setPayableItemIds(List<Long> payableItemIds){this.payableItemIds=payableItemIds;}
}
