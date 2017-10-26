package nc.ui.jzinv.receive.handler;

import nc.ui.jzinv.pub.handler.InvCardEditHandler;
import nc.ui.jzinv.pub.tool.InvMnyTool;
import nc.ui.pub.bill.BillCardPanel;
import nc.ui.pub.bill.BillEditEvent;
import nc.ui.trade.manage.BillManageUI;
import nc.vo.jzinv.receive.ReceiveBVO;
import nc.vo.jzinv.receive.ReceiveDetailVO;
import nc.vo.jzinv.receive.ReceiveVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDouble;
/**
 * ��Ʊ��˰��༭�¼�
 * @author mayyc
 *
 */
public class NtaxmnyEditHandler extends InvCardEditHandler{

	public NtaxmnyEditHandler(BillManageUI clientUI) {
		super(clientUI);
	}
	
	/**
	 * ��Ʊ��ͷ��˰��༭���¼�
	 */
    @Override
	public void cardHeadAfterEdit(BillEditEvent e) {
    	if(ReceiveVO.NTAXMNY.equals(e.getKey())){
    		if(!checkBisValid(e)){
    			return;
    		}
    		BillCardPanel cardPanel = getClientUI().getBillCardPanel();
			String[] taxmnyFields = new String[]{ReceiveVO.NINVTAXMNY};
			String[] mnyFields = new String[]{ReceiveVO.NINVMNY};
			InvMnyTool.computeMnyByTaxmny(ReceiveVO.NTAXMNY, taxmnyFields, mnyFields, cardPanel, e);
			// linan add ����ʣ����˰��
			ReceiveEditTool.setNsurplussplittax(getClientUI());
    	}
	}

    /**
     * ���˰���Ƿ�Ϸ�
     * @param e
     * @return
     */
    private boolean checkBisValid(BillEditEvent e){
    	UFBoolean bisRed = new UFBoolean((String)getCardPanel().getHeadItem(ReceiveVO.BISRED).getValueObject());
    	UFDouble ntaxmny =  new UFDouble((String)e.getValue());
    	if(bisRed.booleanValue()){   		
    		if(ntaxmny.compareTo(UFDouble.ZERO_DBL) > 0){
    			setOldTaxmnyValue(e);
    			getClientUI().showErrorMessage("����ƱΪ��Ʊ, ˰�����Ϊ����, ����������!");
		    	return false;
    		}
    	}
    	else if(ntaxmny.compareTo(UFDouble.ZERO_DBL) < 0){
    		setOldTaxmnyValue(e);
    		getClientUI().showErrorMessage("����ƱΪ��Ʊ, ˰�����Ϊ����, ����������!");
	    	return false;
    	}
    	return true;
    }
	/**
	 * ��Ʊ���塰˰��༭���¼�
	 */
	@Override
	public void cardBodyAfterEdit(BillEditEvent e) {
		if(ReceiveBVO.NTAXMNY.equals(e.getKey())){
			if(!checkBisValid(e)){    			return;
    		}
			BillCardPanel cardPanel = getClientUI().getBillCardPanel();
			String curTabcode = cardPanel.getCurrentBodyTableCode();//��ǰҳǩ
			if(curTabcode.equals(ReceiveBVO.TABCODE)){
				String[] taxmnyFields = new String[]{ReceiveVO.NINVTAXMNY};
				String[] mnyFields = new String[]{ReceiveVO.NINVMNY};
				InvMnyTool.computeMnyByTaxmny(ReceiveVO.NTAXMNY, taxmnyFields, mnyFields, cardPanel, e);
			}
			else if(curTabcode.equals(ReceiveDetailVO.TABCODE)){
				String[] taxmnyFields = new String[]{ReceiveDetailVO.NTHRECETAXMNY};
				String[] mnyFields = new String[]{ReceiveDetailVO.NTHRECEMNY};
				InvMnyTool.computeMnyByTaxmny(ReceiveVO.NTAXMNY, taxmnyFields, mnyFields, cardPanel, e);
			}
		}
	}
	private void setOldTaxmnyValue(BillEditEvent e){
    	if(e.getPos() == 1){
			getCardPanel().setBodyValueAt(e.getOldValue(), e.getRow(), ReceiveBVO.NTAXMNY);
		}
		else{
			getCardPanel().setHeadItem(ReceiveVO.NTAXMNY, e.getOldValue());
		}
    }
}