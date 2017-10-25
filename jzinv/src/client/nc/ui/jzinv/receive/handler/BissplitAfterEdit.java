package nc.ui.jzinv.receive.handler;

import nc.vo.jzinv.receive.ReceiveVO;
import nc.vo.pub.lang.UFBoolean;
import nc.ui.trade.manage.BillManageUI;

public class BissplitAfterEdit {
	/** 
	* @Title: bIsSplitAfterEdit 
	* @Description: 是否拆分修改事件 
	* @param @param bissplit    
	* @return void    
	* @throws 
	*/
	public void bIsSplitAfterEditSetData(UFBoolean bissplit, BillManageUI billManagerUI) {
		if(!bissplit.booleanValue()){
			//清空相关字段 
			billManagerUI.getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTMNY).setValue(null);
			billManagerUI.getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTTAXMNY).setValue(null);
			billManagerUI.getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICETAX).setValue(null);
			billManagerUI.getBillCardPanel().getHeadItem(ReceiveVO.NCUMULATIVESPLITTAX).setValue(null);
			billManagerUI.getBillCardPanel().getHeadItem(ReceiveVO.NSURPLUSSPLITTAX).setValue(null);
		}
		//设置是否必填
		billManagerUI.getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTMNY).setNull(bissplit.booleanValue());
		billManagerUI.getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTTAXMNY).setNull(bissplit.booleanValue());
		billManagerUI.getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICETAX).setNull(bissplit.booleanValue());
		//设置字段的编辑性
		billManagerUI.getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTMNY).setEdit(bissplit.booleanValue());
		billManagerUI.getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTTAXMNY).setEdit(bissplit.booleanValue());
		billManagerUI.getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICETAX).setEdit(bissplit.booleanValue());	
	}	
}
