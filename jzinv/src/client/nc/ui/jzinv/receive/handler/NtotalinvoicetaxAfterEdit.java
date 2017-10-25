package nc.ui.jzinv.receive.handler;

import nc.ui.jzinv.pub.handler.InvCardEditHandler;
import nc.ui.pub.bill.BillEditEvent;
import nc.ui.trade.manage.BillManageUI;
import nc.vo.jzinv.receive.ReceiveVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDouble;

/**
 * @ClassName: NtotalinvoicetaxAfterEdit
 * @Description: Ʊ����˰��ı�
 * @author linan linanb@yonyou.com
 * @date 2017-10-25 ����7:31:19
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
			// ���ûѡ�еĻ����Ͳ������߼�
			if (UFBoolean.TRUE.equals(bissplit)) {
				//��˰��
				UFDouble ntotalinvoicetax = e.getValue() == null ? UFDouble.ZERO_DBL
						: new UFDouble(e.getValue().toString());
				//���β��˰��
				UFDouble ntaxmny = getClientUI().getBillCardPanel()
						.getHeadItem(ReceiveVO.NTAXMNY).getValueObject() == null ? UFDouble.ZERO_DBL
						: new UFDouble(getClientUI().getBillCardPanel()
								.getHeadItem(ReceiveVO.NTAXMNY)
								.getValueObject().toString());
				//�����ۼ��Ѳ�ֽ��
				UFDouble sumTax = null; //todo
				//�����ۼ��Ѳ�ֽ��
				getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NCUMULATIVESPLITTAX).setValue(sumTax);
				//����ʣ����˰��
				getClientUI().getBillCardPanel()
						.getHeadItem(ReceiveVO.NSURPLUSSPLITTAX).setValue(ntotalinvoicetax.sub(ntaxmny).sub(sumTax));		
			}
		}
	}

}
