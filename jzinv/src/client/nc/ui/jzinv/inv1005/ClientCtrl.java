package nc.ui.jzinv.inv1005;

import nc.ui.jzinv.receive.ReceiveCtrl;
import nc.vo.jzinv.pub.IJzinvBillType;

public class ClientCtrl extends ReceiveCtrl{

	@Override
	public String getBillType() {
		return IJzinvBillType.JZINV_RECEIVE_MT;
	}
}
