package com.team.delivery.DTO;

import lombok.Data;

@Data
public class orderDTO {
    private int oSeqno;
    private int sSe;
    private String mId;
    private int oPrice;
    private String oDate;
    private String oName;
    private int cntReview;
    private String oStatus;
    private String sName;

    //memberDTO
    private String mName;
    private String mMobile;

    //o_detailed table
    private int oSe;
    private int oQty;
    private int qtyPrice;
    private int hab;
}
