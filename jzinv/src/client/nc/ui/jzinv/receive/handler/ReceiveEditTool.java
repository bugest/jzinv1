package nc.ui.jzinv.receive.handler;

import java.util.List;

import nc.vo.jzinv.receive.ReceiveVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDouble;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.jzinv.receive.IReceiveService;
import nc.ui.trade.manage.BillManageUI;

/** 
* @ClassName: ReceiveEditTool 
* @Description: �޸ĺ����һЩ��������
* @author linan linanb@yonyou.com 
* @date 2017-10-26 ����10:27:23 
*  
*/
public class ReceiveEditTool {
	/** 
	* @Title: setNsurplussplittax 
	* @Description: ����ʣ����˰���� 
	* @param     
	* @return void    
	* @throws 
	*/
	public static void setNsurplussplittax(BillManageUI billManageUI) {
		//�Ƿ�ѡ���˲�ֽ��
		UFBoolean bissplit = new UFBoolean(billManageUI.getBillCardPanel()
				.getHeadItem(ReceiveVO.BISSPLIT).getValueObject()
				.toString());
		//ֻ��ѡ�в�ֲŻ����ʣ����˰��
		if (UFBoolean.TRUE.equals(bissplit)) {
			// ��˰��
			UFDouble ntotalinvoicetax = billManageUI.getBillCardPanel()
					.getHeadItem(ReceiveVO.NTOTALINVOICETAX).getValueObject() == null ? UFDouble.ZERO_DBL
					: new UFDouble(billManageUI.getBillCardPanel()
							.getHeadItem(ReceiveVO.NTOTALINVOICETAX)
							.getValueObject().toString());
			// ���β��˰��
			UFDouble ntaxmny = billManageUI.getBillCardPanel()
					.getHeadItem(ReceiveVO.NTAXMNY).getValueObject() == null ? UFDouble.ZERO_DBL
					: new UFDouble(billManageUI.getBillCardPanel()
							.getHeadItem(ReceiveVO.NTAXMNY)
							.getValueObject().toString());
			// ��ǰ�ı�id
			String pk_receive = (String) billManageUI.getBillCardPanel()
					.getHeadItem(ReceiveVO.PK_RECEIVE).getValueObject();
			String vinvcode = (String) billManageUI.getBillCardPanel()
					.getHeadItem(ReceiveVO.VINVCODE).getValueObject();
			String vinvno = (String) billManageUI.getBillCardPanel()
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
				billManageUI.showErrorMessage("��ѯ��Ʊ����������!");
			}
			// �����ۼ��Ѳ�ֽ��
			billManageUI.getBillCardPanel()
					.getHeadItem(ReceiveVO.NCUMULATIVESPLITTAX)
					.setValue(sumTax);
			// ����ʣ����˰��
			billManageUI.getBillCardPanel()
					.getHeadItem(ReceiveVO.NSURPLUSSPLITTAX)
					.setValue(ntotalinvoicetax.sub(ntaxmny).sub(sumTax));			
		}
	}
	
	
	/** 
	* @Title: setNsurplussplittax 
	* @Description: ����ʣ����˰����  ����˰������ ��˰-����˰
	* @param     
	* @return void    
	* @throws 
	*/
	public static void setNsurplussplittaxBySubTax(BillManageUI billManageUI) {
		//�Ƿ�ѡ���˲�ֽ��
		UFBoolean bissplit = new UFBoolean(billManageUI.getBillCardPanel()
				.getHeadItem(ReceiveVO.BISSPLIT).getValueObject()
				.toString());
		//ֻ��ѡ�в�ֲŻ����ʣ����˰��
		if (UFBoolean.TRUE.equals(bissplit)) {
			// ��˰��
			UFDouble ntotalinvoicetax = billManageUI.getBillCardPanel()
					.getHeadItem(ReceiveVO.NTOTALINVOICETAX).getValueObject() == null ? UFDouble.ZERO_DBL
					: new UFDouble(billManageUI.getBillCardPanel()
							.getHeadItem(ReceiveVO.NTOTALINVOICETAX)
							.getValueObject().toString());
			// ���β��˰��
			UFDouble ntaxmny = billManageUI.getBillCardPanel()
					.getHeadItem(ReceiveVO.NINVTAXMNY).getValueObject() == null ? UFDouble.ZERO_DBL
					: new UFDouble(billManageUI.getBillCardPanel()
							.getHeadItem(ReceiveVO.NINVTAXMNY)
							.getValueObject().toString()).sub(
									billManageUI.getBillCardPanel()
									.getHeadItem(ReceiveVO.NINVMNY).getValueObject() == null ? UFDouble.ZERO_DBL
									: new UFDouble(billManageUI.getBillCardPanel()
											.getHeadItem(ReceiveVO.NINVMNY)
											.getValueObject().toString()) );
			// ��ǰ�ı�id
			String pk_receive = (String) billManageUI.getBillCardPanel()
					.getHeadItem(ReceiveVO.PK_RECEIVE).getValueObject();
			String vinvcode = (String) billManageUI.getBillCardPanel()
					.getHeadItem(ReceiveVO.VINVCODE).getValueObject();
			String vinvno = (String) billManageUI.getBillCardPanel()
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
				billManageUI.showErrorMessage("��ѯ��Ʊ����������!");
			}
			// �����ۼ��Ѳ�ֽ��
			billManageUI.getBillCardPanel()
					.getHeadItem(ReceiveVO.NCUMULATIVESPLITTAX)
					.setValue(sumTax);
			// ����ʣ����˰��
			billManageUI.getBillCardPanel()
					.getHeadItem(ReceiveVO.NSURPLUSSPLITTAX)
					.setValue(ntotalinvoicetax.sub(ntaxmny).sub(sumTax));			
		}
	}
	
	/** 
	* @Title: bIsSplitAfterEdit 
	* @Description: �Ƿ����޸��¼� 
	* @param @param bissplit    
	* @return void    
	* @throws 
	*/
	public static void bIsSplitAfterEditSetData(UFBoolean bissplit, BillManageUI billManagerUI) {
/*		if(!bissplit.booleanValue()){
			//�������ֶ� 
			billManagerUI.getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTMNY).setValue(null);
			billManagerUI.getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTTAXMNY).setValue(null);
			billManagerUI.getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICETAX).setValue(null);
			billManagerUI.getBillCardPanel().getHeadItem(ReceiveVO.NCUMULATIVESPLITTAX).setValue(null);
			billManagerUI.getBillCardPanel().getHeadItem(ReceiveVO.NSURPLUSSPLITTAX).setValue(null);
		}*/
		//����ѡ�кͲ�ѡ������գ�������߼������߼�
		billManagerUI.getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTMNY).setValue(null);
		billManagerUI.getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTTAXMNY).setValue(null);
		billManagerUI.getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICETAX).setValue(null);
		billManagerUI.getBillCardPanel().getHeadItem(ReceiveVO.NCUMULATIVESPLITTAX).setValue(null);
		billManagerUI.getBillCardPanel().getHeadItem(ReceiveVO.NSURPLUSSPLITTAX).setValue(null);
		//�����Ƿ����
		billManagerUI.getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTMNY).setNull(bissplit.booleanValue());
		billManagerUI.getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTTAXMNY).setNull(bissplit.booleanValue());
		billManagerUI.getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICETAX).setNull(bissplit.booleanValue());
		//�����ֶεı༭��
		billManagerUI.getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTMNY).setEdit(bissplit.booleanValue());
		billManagerUI.getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTTAXMNY).setEdit(bissplit.booleanValue());
		billManagerUI.getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICETAX).setEdit(bissplit.booleanValue());	
	}	
}
