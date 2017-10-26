package nc.ui.jzinv.inv1010;

import nc.ui.trade.manage.BillManageUI;
import nc.ui.jzinv.receive.ReceiveEH;
import nc.ui.trade.controller.IControllerBase;
import nc.vo.pub.SuperVO;

public class EventHandler extends ReceiveEH{

	public EventHandler(BillManageUI billUI, IControllerBase control) {
		super(billUI, control);
	}

	@Override
	protected void onBoQuery() throws Exception {
		StringBuffer strWhere = new StringBuffer();

		if (askForQueryCondition(strWhere) == false)
			return;// 用户放弃了查询
		strWhere.append("and (jzinv_receive.pk_billtype = '99Z4' )");
		SuperVO[] queryVos = queryHeadVOs(strWhere.toString());

		getBufferData().clear();
		// 增加数据到Buffer
		addDataToBuffer(queryVos);

		updateBuffer();
	}
	@Override
	public void onBoAudit() throws Exception {
		super.onBoAudit();
		super.onBoRefresh();
	}

	@Override
	protected void onBoCancelAudit() throws Exception {
		super.onBoCancelAudit();
		super.onBoRefresh();
	}
}
