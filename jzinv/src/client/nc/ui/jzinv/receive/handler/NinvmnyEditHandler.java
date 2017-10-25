package nc.ui.jzinv.receive.handler;

import nc.ui.jzinv.pub.handler.InvCardEditHandler;
import nc.ui.pub.bill.BillEditEvent;
import nc.ui.trade.manage.BillManageUI;
import nc.vo.jzinv.receive.ReceiveBVO;
import nc.vo.jzinv.receive.ReceiveDetailVO;
import nc.vo.jzinv.receive.ReceiveVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDouble;

/**
 * ��Ʊ����Ʊ���༭�¼�
 * @author mayyc
 *
 */
public class NinvmnyEditHandler extends InvCardEditHandler{

	public NinvmnyEditHandler(BillManageUI clientUI) {
		super(clientUI);
	}
	/**
	 * ��Ʊ��ͷ����Ʊ���༭���¼�
	 */
	public void cardHeadAfterEdit(BillEditEvent e) {
		if(ReceiveVO.NINVMNY.equals(e.getKey()) || ReceiveVO.NINVTAXMNY.equals(e.getKey())){
			UFBoolean bisred = new UFBoolean((String)getClientUI().getBillCardPanel().
					getHeadItem(ReceiveVO.BISRED).getValueObject());
			UFDouble nbaseinvmny = new UFDouble((String)e.getValue());
			if(bisred.booleanValue() && nbaseinvmny.compareTo(UFDouble.ZERO_DBL) > 0){
				getClientUI().getBillCardPanel().setHeadItem( ReceiveBVO.NINVMNY, null);
				getClientUI().getBillCardPanel().setHeadItem( ReceiveBVO.NINVTAXMNY, null);
				getClientUI().showErrorMessage("�÷�ƱΪ��Ʊ, ������Ϊ��, ����������!");
				return;
			}
			else{
				if(nbaseinvmny.compareTo(UFDouble.ZERO_DBL) < 0 && !bisred.booleanValue()){
					getClientUI().getBillCardPanel().setHeadItem( ReceiveBVO.NINVMNY, null);
					getClientUI().getBillCardPanel().setHeadItem( ReceiveBVO.NINVTAXMNY, null);
					getClientUI().showErrorMessage("�÷�ƱΪ��Ʊ, ������Ϊ��, ����������!");
					return;
				}
			}
			super.cardHeadAfterEdit(e);
			int rows = getClientUI().getBillCardPanel().getBillModel(ReceiveDetailVO.TABCODE).getRowCount();
			if(rows == 0){
				setNewLineMny(e);			
			}
			setBodyMnyValue(e);
		}
	}

	/**
	 * ��Ʊ���塰��Ʊ���༭���¼�
	 */
	@Override
    public void cardBodyAfterEdit(BillEditEvent e){
    	if(ReceiveBVO.NINVMNY.equals(e.getKey()) || ReceiveBVO.NINVTAXMNY.equals(e.getKey())){
			UFBoolean bisred = new UFBoolean((String)getClientUI().getBillCardPanel().
					getHeadItem(ReceiveVO.BISRED).getValueObject());
			UFDouble nbaseinvmny = new UFDouble((String)e.getValue());
			if(nbaseinvmny.compareTo(UFDouble.ZERO_DBL) > 0 && bisred.booleanValue()){
				super.setOldValue(e);
				getClientUI().showErrorMessage("�÷�ƱΪ��Ʊ, ������Ϊ��, ����������!");
				return;
			}else if(nbaseinvmny.compareTo(UFDouble.ZERO_DBL) < 0 && !bisred.booleanValue()){
				super.setOldValue(e);
				getClientUI().showErrorMessage("�÷�ƱΪ��Ʊ, ������Ϊ��, ����������!");
				return;
			}else{
				super.cardBodyAfterEdit(e);
				//���ܱ����һ��ҳǩ�ķ�Ʊ����ͷ
				//ReceiveUtil.collectMnyToHead(getClientUI());
			}
		}
	}
    
	/**
	 * ���������е�ֵ
	 * @param e
	 */
	private void setNewLineMny(BillEditEvent e){
		//��������, ������Ʊ��ֵ��������Ʊ���
		getClientUI().getBillCardPanel().getBodyPanel(ReceiveDetailVO.TABCODE).addLine();
	}
	
	private void setBodyMnyValue(BillEditEvent e) {
		String pk_project = getHeadString(ReceiveVO.PK_PROJECT);
		String pk_projectbase = getHeadString(ReceiveVO.PK_PROJECTBASE);
		//�жϱ����Ƿ�����Ŀ��û����Ŀ�ڸ�ֵ
		String bodypkProject = getCardPanel().getBillModel(ReceiveDetailVO.TABCODE).getValueAt(0, ReceiveDetailVO.PK_PROJECT)
				==null?"":getCardPanel().getBillModel(ReceiveDetailVO.TABCODE).getValueAt(0, ReceiveDetailVO.PK_PROJECT).toString();
		if("".equals(bodypkProject)){
			getCardPanel().setBodyValueAt(pk_project, 0, 
					ReceiveDetailVO.PK_PROJECT, ReceiveDetailVO.TABCODE);
			getCardPanel().setBodyValueAt(pk_projectbase, 0, 
					ReceiveDetailVO.PK_PROJECTBASE, ReceiveDetailVO.TABCODE);
		}
		
		UFDouble nbaseinvmny = new UFDouble((String)getCardPanel().
				getHeadItem(ReceiveVO.NINVMNY).getValueObject());

		getCardPanel().setBodyValueAt(nbaseinvmny, 0, 
				ReceiveDetailVO.NTHRECEMNY, ReceiveDetailVO.TABCODE);
		UFDouble nbaseinvtaxmny = new UFDouble((String)getCardPanel().
				getHeadItem(ReceiveVO.NINVTAXMNY).getValueObject());
		getCardPanel().setBodyValueAt(nbaseinvtaxmny, 0, 
				ReceiveDetailVO.NTHRECETAXMNY, ReceiveDetailVO.TABCODE);
		UFDouble ntaxrate = new UFDouble((String)getCardPanel().getHeadItem(ReceiveVO.NTAXRATE).getValueObject());
		getCardPanel().setBodyValueAt(ntaxrate, 0, 
				ReceiveDetailVO.NTAXRATE, ReceiveDetailVO.TABCODE);
        UFDouble ntaxmny = new UFDouble((String)getCardPanel().getHeadItem(ReceiveVO.NTAXMNY).getValueObject());
        getCardPanel().setBodyValueAt(ntaxmny, 0, 
				ReceiveDetailVO.NTAXMNY, ReceiveDetailVO.TABCODE);
        
        getCardPanel().getBillModel(ReceiveDetailVO.TABCODE).execLoadFormula();
	}

}