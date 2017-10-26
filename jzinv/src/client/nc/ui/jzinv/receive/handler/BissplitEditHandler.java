package nc.ui.jzinv.receive.handler;

import java.util.List;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.jzinv.receive.IReceiveService;
import nc.ui.jzinv.pub.handler.InvCardEditHandler;
import nc.ui.pub.bill.BillEditEvent;
import nc.ui.trade.manage.BillManageUI;
import nc.vo.jzinv.receive.ReceiveVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDouble;

/**
 * @ClassName: BissplitEditHandler
 * @Description: �Ƿ���
 * @author linan linanb@yonyou.com
 * @date 2017-10-24 ����11:34:45
 * 
 */
public class BissplitEditHandler extends InvCardEditHandler {
	public BissplitEditHandler(BillManageUI clientUI) {
		super(clientUI);
	}

	@Override
	public void cardHeadAfterEdit(BillEditEvent e) {
		if (ReceiveVO.BISSPLIT.equals(e.getKey())) {
			UFBoolean bissplit = new UFBoolean((String) e.getValue());
			ReceiveEditTool.bIsSplitAfterEditSetData(bissplit,
					getClientUI());
			//�����ѡ�в�֣�������ۼƺ�δ��ֽ��
			//���ø���˰��
			setTax(bissplit);	
		}
	}

	/**
	 * @Title: setTax
	 * @Description: ����˰��
	 * @param
	 * @return void
	 * @throws
	 */
	private void setTax(UFBoolean bissplit) {
		// ѡ��ʱ�����ۼƽ��
		if (bissplit.booleanValue()) {
			// ��ǰ�ı�id
			String pk_receive = (String) getClientUI().getBillCardPanel()
					.getHeadItem(ReceiveVO.PK_RECEIVE).getValueObject();
			String vinvcode = (String) getClientUI().getBillCardPanel()
					.getHeadItem(ReceiveVO.VINVCODE).getValueObject();
			String vinvno = (String) getClientUI().getBillCardPanel()
					.getHeadItem(ReceiveVO.VINVNO).getValueObject();
			// �����ۼ�ֵ��ѯ����Ϊ = vinvcode = vinvno <> pk_receive
			try {
				List<ReceiveVO> receiveVOList = NCLocator.getInstance()
						.lookup(IReceiveService.class)
						.querySplitHeadVOsByCond(vinvcode, vinvno, pk_receive);
				UFBoolean isFirst = null;
				UFDouble sumTax = UFDouble.ZERO_DBL;
				if (receiveVOList == null || receiveVOList.isEmpty()) {
					getClientUI().getBillCardPanel()
							.getHeadItem(ReceiveVO.NCUMULATIVESPLITTAX)
							.setValue(sumTax);
					isFirst = UFBoolean.TRUE;
					//�ǵ�һ�β��
				} else {
					//���ǵ�һ�β��
					isFirst = UFBoolean.FALSE;
					for (ReceiveVO receiveVO : receiveVOList) {
						sumTax = sumTax.add(receiveVO.getNtaxmny() == null ? UFDouble.ZERO_DBL : receiveVO.getNtaxmny());
						// ���ǵ�һ�ξ���֮ǰ�����ĸ�����
						getClientUI().getBillCardPanel()
								.getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTMNY)
								.setValue(receiveVO.getNtotalinvoiceamountmny() == null ? UFDouble.ZERO_DBL : receiveVO.getNtotalinvoiceamountmny());
						getClientUI()
								.getBillCardPanel()
								.getHeadItem(
										ReceiveVO.NTOTALINVOICEAMOUNTTAXMNY)
								.setValue(receiveVO.getNtotalinvoiceamounttaxmny() == null ? UFDouble.ZERO_DBL : receiveVO.getNtotalinvoiceamounttaxmny());
						getClientUI().getBillCardPanel()
								.getHeadItem(ReceiveVO.NTOTALINVOICETAX)
								.setValue(receiveVO.getNtotalinvoicetax() == null ? UFDouble.ZERO_DBL : receiveVO.getNtotalinvoicetax());
					}
					getClientUI().getBillCardPanel()
					.getHeadItem(ReceiveVO.NCUMULATIVESPLITTAX)
					.setValue(sumTax);	
				}
				//��һ�ξͿ��Ըı䣬�ڶ��ξͲ��ɸı�
				getClientUI().getBillCardPanel()
						.getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTMNY)
						.setEdit(isFirst.booleanValue());
				getClientUI().getBillCardPanel()
						.getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTTAXMNY)
						.setEdit(isFirst.booleanValue());
				getClientUI().getBillCardPanel()
						.getHeadItem(ReceiveVO.NTOTALINVOICETAX)
						.setEdit(isFirst.booleanValue());
				//����ʣ���ֽ��
				//��˰�� - �Ѿ���� - ���β��
				UFDouble ntotalinvoicetax = getClientUI().getBillCardPanel()
						.getHeadItem(ReceiveVO.NTOTALINVOICETAX).getValueObject() == null ? UFDouble.ZERO_DBL : 
							new UFDouble(getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICETAX).getValueObject().toString()); 
				UFDouble ntaxmny = getClientUI().getBillCardPanel()
						.getHeadItem(ReceiveVO.NTAXMNY).getValueObject() == null ? UFDouble.ZERO_DBL : 
							new UFDouble(getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTAXMNY).getValueObject().toString());
				getClientUI().getBillCardPanel()
				.getHeadItem(ReceiveVO.NSURPLUSSPLITTAX)
				.setValue(ntotalinvoicetax.sub(ntaxmny).sub(sumTax));
			} catch (BusinessException e) {
				Logger.error("��ѯ��Ʊ����������", e);
				getClientUI().showErrorMessage("��ѯ��Ʊ����������!");
			}
		}
	}

}
