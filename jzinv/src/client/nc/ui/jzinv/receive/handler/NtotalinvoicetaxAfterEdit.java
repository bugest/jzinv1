package nc.ui.jzinv.receive.handler;

import nc.ui.jzinv.pub.handler.InvCardEditHandler;
import nc.ui.pub.bill.BillEditEvent;
import nc.ui.trade.manage.BillManageUI;
import nc.vo.jzinv.receive.ReceiveVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDouble;

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
		if (ReceiveVO.NTOTALINVOICETAX.equals(e.getKey())) {
			UFBoolean bissplit = new UFBoolean(getClientUI().getBillCardPanel()
					.getHeadItem(ReceiveVO.BISSPLIT).getValueObject()
					.toString());
			// 如果没选中的话，就不处理逻辑
			if (UFBoolean.TRUE.equals(bissplit)) {
				//总税金
				UFDouble ntotalinvoicetax = e.getValue() == null ? UFDouble.ZERO_DBL
						: new UFDouble(e.getValue().toString());
				//本次拆分税金
				UFDouble ntaxmny = getClientUI().getBillCardPanel()
						.getHeadItem(ReceiveVO.NTAXMNY).getValueObject() == null ? UFDouble.ZERO_DBL
						: new UFDouble(getClientUI().getBillCardPanel()
								.getHeadItem(ReceiveVO.NTAXMNY)
								.getValueObject().toString());
				//设置累计已拆分金额
				UFDouble sumTax = null; //todo
				//设置累计已拆分金额
				getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NCUMULATIVESPLITTAX).setValue(sumTax);
				//设置剩余拆分税金
				getClientUI().getBillCardPanel()
						.getHeadItem(ReceiveVO.NSURPLUSSPLITTAX).setValue(ntotalinvoicetax.sub(ntaxmny).sub(sumTax));		
			}
		}
	}

}
