package com.his.vo;

public class PaymentResponseVo {
    private Long invoiceNo;
    public PaymentResponseVo() {}
    public PaymentResponseVo(Long invoiceNo) { this.invoiceNo = invoiceNo; }
    public Long getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(Long invoiceNo) { this.invoiceNo = invoiceNo; }
}
