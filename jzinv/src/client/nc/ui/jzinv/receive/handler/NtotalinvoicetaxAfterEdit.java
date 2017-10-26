package nc.ui.jzinv.receive.handler;

import nc.ui.jzinv.pub.handler.InvCardEditHandler;
import nc.ui.pub.bill.BillEditEvent;
import nc.ui.trade.manage.BillManageUI;
import nc.vo.jzinv.receive.ReceiveVO;

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
			ReceiveEditTool.setNsurplussplittax(getClientUI());
			/*UFBoolean bissplit = new UFBoolean(getClientUI().getBillCardPanel()
					.getHeadItem(ReceiveVO.BISSPLIT).getValueObject()
					.toString());
			// ���ûѡ�еĻ����Ͳ������߼�
			if (UFBoolean.TRUE.equals(bissplit)) {
				// ��˰��
				UFDouble ntotalinvoicetax = e.getValue() == null ? UFDouble.ZERO_DBL
						: new UFDouble(e.getValue().toString());
				// ���β��˰��
				UFDouble ntaxmny = getClientUI().getBillCardPanel()
						.getHeadItem(ReceiveVO.NTAXMNY).getValueObject() == null ? UFDouble.ZERO_DBL
						: new UFDouble(getClientUI().getBillCardPanel()
								.getHeadItem(ReceiveVO.NTAXMNY)
								.getValueObject().toString());
				// ��ǰ�ı�id
				String pk_receive = (String) getClientUI().getBillCardPanel()
						.getHeadItem(ReceiveVO.PK_RECEIVE).getValueObject();
				String vinvcode = (String) getClientUI().getBillCardPanel()
						.getHeadItem(ReceiveVO.VINVCODE).getValueObject();
				String vinvno = (String) getClientUI().getBillCardPanel()
						.getHeadItem(ReceiveVO.VINVNO).getValueObject();
				// �����ۼ��Ѳ�ֽ��
				UFDouble sumTax = UFDouble.ZERO_DBL; // todo
				try {
					List<ReceiveVO> receiveVOList = NCLocator
							.getInstance()
							.lookup(IReceiveService.class)
							.querySplitHeadVOsByCond(vinvcode, vinvno,
									pk_receive);
					if (receiveVOList != null && !receiveVOList.isEmpty()) {
						for (ReceiveVO receiveVO : receiveVOList) {
							sumTax = sumTax.add(receiveVO.getNtaxmny() == null ? UFDouble.ZERO_DBL : receiveVO.getNtaxmny());
						}
					}
				} catch (BusinessException be) {
					Logger.error("��ѯ��Ʊ����������", be);
					getClientUI().showErrorMessage("��ѯ��Ʊ����������!");
				}
				// �����ۼ��Ѳ�ֽ��
				getClientUI().getBillCardPanel()
						.getHeadItem(ReceiveVO.NCUMULATIVESPLITTAX)
						.setValue(sumTax);
				// ����ʣ����˰��
				getClientUI().getBillCardPanel()
						.getHeadItem(ReceiveVO.NSURPLUSSPLITTAX)
						.setValue(ntotalinvoicetax.sub(ntaxmny).sub(sumTax));
			}*/
		}
	}

}
