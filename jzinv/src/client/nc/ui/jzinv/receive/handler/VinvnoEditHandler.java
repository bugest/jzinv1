package nc.ui.jzinv.receive.handler;

import nc.ui.jzinv.pub.handler.InvCardEditHandler;
import nc.ui.pub.bill.BillEditEvent;
import nc.ui.trade.manage.BillManageUI;
import nc.vo.jzinv.receive.ReceiveVO;
import nc.vo.pub.lang.UFBoolean;

/** 
* @ClassName: VinvnoEditHandler 
* @Description: ��Ʊ��
* @author linan linanb@yonyou.com 
* @date 2017-10-24 ����1:55:24 
*  
*/
public class VinvnoEditHandler extends InvCardEditHandler{
	public VinvnoEditHandler(BillManageUI clientUI) {
		super(clientUI);
	}
	@Override
	public void cardHeadAfterEdit(BillEditEvent e) {
		if(ReceiveVO.VINVNO.equals(e.getKey())){
			//����VINVNO��VINVCODE�����Ƿ��ֵ�״̬
			//ֻҪ�ı䣬�������ò����
			getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.BISSPLIT).setValue(false);
			ReceiveEditTool.bIsSplitAfterEditSetData(UFBoolean.FALSE, getClientUI());
			String vinvcode = (String) getCardPanel().getHeadItem(ReceiveVO.VINVCODE).getValueObject();
			String vinvno = (String) getCardPanel().getHeadItem(ReceiveVO.VINVNO).getValueObject();
			//�����code��no��һ��Ϊ�վͣ��Ͳ���ѡ���֣����ܱ༭
			if(vinvcode == null || vinvcode.trim().equals("") || vinvno == null || vinvno.trim().equals("")) {
				getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.BISSPLIT).setEdit(false);
			} else {
				getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.BISSPLIT).setEdit(true);
			}			
		}
	}
	
}
