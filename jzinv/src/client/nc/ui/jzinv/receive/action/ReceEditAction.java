package nc.ui.jzinv.receive.action;

import java.util.List;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.jzinv.receive.IReceiveService;
import nc.ui.jzinv.pub.action.InvoiceAction;
import nc.ui.pub.bill.BillCardPanel;
import nc.ui.trade.manage.BillManageUI;
import nc.vo.jzinv.pub.IJzinvBillType;
import nc.vo.jzinv.receive.ReceiveVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;

/**
 * ��Ʊ�޸İ�ť
 * @author mayyc
 *
 */
public class ReceEditAction extends InvoiceAction{

	public ReceEditAction(BillManageUI clientUI) {
		super(clientUI);
	}

	@Override
	public void doAction() throws Exception {
		setHeadValue();
		//����༭����
		setTaxSplitFields();
	}
    private void setHeadValue(){
    	BillCardPanel cardPanel = getClientUI().getBillCardPanel();
		UFBoolean bisopenred  = new UFBoolean((String)cardPanel.getHeadItem(ReceiveVO.BISOPENRED).getValueObject());
		getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.PK_PROJECT).setEdit(!bisopenred.booleanValue());
		getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.PK_SUPPLIER).setEdit(!bisopenred.booleanValue());
		
		ReceiveVO headvo = (ReceiveVO)getClientUI().getBufferData().getCurrentVO().getParentVO();
		UFBoolean bisred = headvo.getBisred();
		if(bisred.booleanValue()){
			getClientUI().getBillCardPanel().getHeadTabbedPane().setSelectedIndex(1);		
		}
		else{
			getClientUI().getBillCardPanel().getHeadTabbedPane().setSelectedIndex(0);		
		}
		getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.PK_RECEIVE_REF).setEdit(false);
		getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.PK_RECEIVE_REF).setNull(bisred.booleanValue());
	
		getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.IREDAPLYREASON).setEdit(bisred.booleanValue());
		getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.IREDAPLYREASON).setNull(bisred.booleanValue());
		
		getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.VREDINFONO).setEdit(bisred.booleanValue());
		getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.VREDINFONO).setNull(bisred.booleanValue());
		
		String pk_corp = getClientUI()._getCorp().getPk_corp();
		boolean bisupload = nc.vo.jzinv.param.InvParamTool.isToGLByBilltype(pk_corp, IJzinvBillType.JZINV_RECEIVE_MT);
		getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.BISUPLOAD).setValue(bisupload);
    }
    
    /** 
    * @Title: setTaxSplitFields 
    * @Description: TODO 
    * @param     
    * @return void    
    * @throws 
    */
    private void setTaxSplitFields() {
		String vinvcode = (String)getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.VINVCODE).getValueObject();
		String vinvno = (String)getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.VINVNO).getValueObject();
		//�����Ʊ�źͷ�Ʊcode��һ��Ϊ�վͲ��ܱ༭���������Ӧ�ò��ᷢ�������ǻ����ж��£��Է���һ
		if(vinvcode == null || vinvcode.trim().equals("") || vinvno == null || vinvno.trim().equals("")) {
			//���÷Ǳ���
			getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTMNY).setNull(false);
			getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTTAXMNY).setNull(false);
			getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICETAX).setNull(false);
			//�����ֶεı༭�� �����ɱ༭
			getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTMNY).setEdit(false);
			getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTTAXMNY).setEdit(false);
			getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICETAX).setEdit(false);	
			getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.BISSPLIT).setEdit(false);
			return;
		}
		UFBoolean bissplit = new UFBoolean(getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.BISSPLIT).getValueObject().toString());
		String pk_receive = (String)getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.PK_RECEIVE).getValueObject();
		//ֻҪ��Ʊ��λ�գ�����ֶξͿ�����д
		getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.BISSPLIT).setEdit(true);
		//�����Ʊ��no code���У��ͷ�������ÿɱ༭��
		//���ѡ��ʱ�����ǿ��Ա༭�ģ������Ƿǿյ�
		if(bissplit.equals(UFBoolean.TRUE)) {	
			try {
				List<ReceiveVO> receiveVOList = NCLocator.getInstance()
						.lookup(IReceiveService.class)
						.querySplitHeadVOsByCond(vinvcode, vinvno, pk_receive);
				//�ж��ǲ��Ƕ��ڱ���Ʊֻ��һ������
				if(receiveVOList == null || receiveVOList.isEmpty()) {
					getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTMNY).setEdit(bissplit.booleanValue());
					getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTTAXMNY).setEdit(bissplit.booleanValue());
					getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICETAX).setEdit(bissplit.booleanValue());	
					getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.BISSPLIT).setEdit(bissplit.booleanValue());
					getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTTAXMNY).setNull(bissplit.booleanValue());
					getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICETAX).setNull(bissplit.booleanValue());
				} else {
					getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTMNY).setEdit(false);
					getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTTAXMNY).setEdit(false);
					getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICETAX).setEdit(false);	
					getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTMNY).setNull(bissplit.booleanValue());
					getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTTAXMNY).setNull(bissplit.booleanValue());
					getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICETAX).setNull(bissplit.booleanValue());					
				}
			} catch (BusinessException e) {
				Logger.error("��ѯ��Ʊ����������", e);
				getClientUI().showErrorMessage("��ѯ��Ʊ����������!");
			}

		} else {
			getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTMNY).setEdit(bissplit.booleanValue());
			getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTTAXMNY).setEdit(bissplit.booleanValue());
			getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICETAX).setEdit(bissplit.booleanValue());	
			getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTMNY).setNull(bissplit.booleanValue());
			getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTTAXMNY).setNull(bissplit.booleanValue());
			getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICETAX).setNull(bissplit.booleanValue());			
		}
		

	}
}