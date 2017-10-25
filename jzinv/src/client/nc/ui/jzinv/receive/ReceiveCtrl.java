package nc.ui.jzinv.receive;

import nc.ui.jzinv.pub.button.IJZPubButton;
import nc.ui.jzinv.pub.button.sort.JZPMPubButtonSort;
import nc.ui.trade.bill.AbstractManageController;
import nc.ui.trade.button.IBillButton;
import nc.vo.jzinv.pub.IJzinvButton;
import nc.vo.jzinv.receive.AggReceiveVO;
import nc.vo.jzinv.receive.ReceiveBVO;
import nc.vo.jzinv.receive.ReceiveDetailVO;
import nc.vo.jzinv.receive.ReceiveVO;

public class ReceiveCtrl  extends AbstractManageController{

	public String[] getCardBodyHideCol() {
		return null;
	}

	public int[] getCardButtonAry() {
		  int[] m_cardBtns = new int[] {
					IBillButton.Refbill,  //≤Œ’’
					IBillButton.Add,
					IJzinvButton.INV_RED, //∫Ï∆±
					IBillButton.Edit,
					IBillButton.Save,
					IBillButton.Cancel,
					IBillButton.Line,
					IBillButton.Del,
					IBillButton.Query,
					IBillButton.Brow,
					IBillButton.Action,
					IBillButton.Print,
					IBillButton.File,
					IJZPubButton.ASSQRY,
					IBillButton.Refresh,
					IBillButton.Return
			};
		  return JZPMPubButtonSort.getInstance().sort(m_cardBtns);
	}

	public boolean isShowCardRowNo() {
		return true;
	}

	public boolean isShowCardTotal() {
		return true;
	}

	public String getBillType() {
		return null;
	}

	public String[] getBillVoName() {
		return new String[]{
				AggReceiveVO.class.getName(),
				ReceiveVO.class.getName(),			
				ReceiveBVO.class.getName(),	
				ReceiveDetailVO.class.getName()
			};
	}

	public String getBodyCondition() {
		return null;
	}

	public String getBodyZYXKey() {
		return null;
	}

	public int getBusinessActionType() {
		return 0;
	}

	public String getChildPkField() {
		return null;
	}

	public String getHeadZYXKey() {
		return null;
	}

	public String getPkField() {
		return ReceiveVO.PK_RECEIVE;
	}

	public Boolean isEditInGoing() throws Exception {
		return null;
	}

	public boolean isExistBillStatus() {
		return true;
	}

	public boolean isLoadCardFormula() {
		return true;
	}

	public String[] getListBodyHideCol() {
		return null;
	}

	public int[] getListButtonAry() {
		 int[] m_listBtns = new int[]{
					IBillButton.Refbill,  //≤Œ’’
					IBillButton.Add,
					IJzinvButton.INV_RED, //∫Ï∆±
					IBillButton.Edit,
					IBillButton.Save,
					IBillButton.Cancel,
					IBillButton.Line,
					IBillButton.Del,
					IBillButton.Query,
					IBillButton.Action,
					IBillButton.File,
					IJZPubButton.ASSQRY,
					IBillButton.Refresh,
					IBillButton.Card
			};
			 return JZPMPubButtonSort.getInstance().sort(m_listBtns);
	}

	public String[] getListHeadHideCol() {
		return null;
	}

	public boolean isShowListRowNo() {
		return true;
	}

	public boolean isShowListTotal() {
		return true;
	}

}