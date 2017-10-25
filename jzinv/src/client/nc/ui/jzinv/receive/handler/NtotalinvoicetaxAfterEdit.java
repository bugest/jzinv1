package nc.ui.jzinv.receive.handler;

import nc.ui.jzinv.pub.handler.InvCardEditHandler;
import nc.ui.pub.bill.BillEditEvent;
import nc.ui.trade.manage.BillManageUI;
import nc.vo.jzinv.receive.ReceiveVO;
import nc.vo.pub.lang.UFBoolean;

/** 
* @ClassName: NtotalinvoicetaxAfterEdit 
* @Description: 票面总税金改变
* @author linan linanb@yonyou.com 
* @date 2017-10-25 下午7:31:19 
*  
*/
public class NtotalinvoicetaxAfterEdit extends InvCardEditHandler {
	public NtotalinvoicetaxAfterEdit(BillManageUI clientUI) {
		super(clientUI);
	}
	@Override
	public void cardHeadAfterEdit(BillEditEvent e) {
		if (ReceiveVO.BISSPLIT.equals(e.getKey())) {
			UFBoolean bissplit = new UFBoolean((String) e.getValue());
			new BissplitAfterEdit().bIsSplitAfterEditSetData(bissplit,
					getClientUI());
		}
	}

}
