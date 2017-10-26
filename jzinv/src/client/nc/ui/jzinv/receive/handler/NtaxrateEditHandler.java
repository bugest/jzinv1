package nc.ui.jzinv.receive.handler;

import nc.bs.logging.Logger;
import nc.itf.jzinv.pub.taxrate.util.JZTaxRateUtil;
import nc.ui.jzinv.pub.handler.InvCardEditHandler;
import nc.ui.jzinv.pub.tool.InvMnyTool;
import nc.ui.pub.bill.BillCardPanel;
import nc.ui.pub.bill.BillEditEvent;
import nc.ui.pub.bill.BillModel;
import nc.ui.trade.manage.BillManageUI;
import nc.vo.jzinv.param.InvParamTool;
import nc.vo.jzinv.pub.utils.SafeCompute;
import nc.vo.jzinv.receive.ReceiveBVO;
import nc.vo.jzinv.receive.ReceiveDetailVO;
import nc.vo.jzinv.receive.ReceiveVO;
import nc.vo.pub.lang.UFDouble;

/**
 * 收票“税率”编辑事件
 * @author mayyc
 *
 */
public class NtaxrateEditHandler extends InvCardEditHandler{

	public NtaxrateEditHandler(BillManageUI clientUI) {
		super(clientUI);
	}

	/**
	 * 收票表头“税率”编辑后事件
	 */
	@Override
	public void cardHeadAfterEdit(BillEditEvent e) {
		if(ReceiveVO.NTAXRATE.equals(e.getKey())){
			try {
				doBodyWhenEditHeadRate(e);
			} catch (Exception ex) {
				Logger.error("", ex);
			}	
			// linan add 设置剩余拆分税金
			ReceiveEditTool.setNsurplussplittax(getClientUI());
			//ReceiveEditTool.setNsurplussplittaxBySubTax(getClientUI());	
		}

	}
	private void doBodyWhenEditHeadRate(BillEditEvent e){
		BillCardPanel cardPanel = getClientUI().getBillCardPanel();
		String[] taxmnyFields = new String[]{ReceiveVO.NINVTAXMNY};
		String[] mnyFields = new String[]{ReceiveVO.NINVMNY};
		InvMnyTool.computeMnyByTaxrate(ReceiveVO.NTAXRATE, taxmnyFields, mnyFields, cardPanel, e);
		UFDouble ntaxrate = new UFDouble((String)this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTAXRATE).getValueObject());
		int rowCount = this.getClientUI().getBillCardPanel().getBillModel("jzinv_receive_detail").getRowCount();			
		if(rowCount > 0){
			UFDouble ninvmny = getHeadUFDouble(ReceiveVO.NINVMNY);
			UFDouble ninvtaxmny = getHeadUFDouble(ReceiveVO.NINVTAXMNY);
			this.getClientUI().getBillCardPanel().setBodyValueAt(ninvmny, 0, ReceiveDetailVO.NTHRECEMNY);
			this.getClientUI().getBillCardPanel().setBodyValueAt(ninvtaxmny, 0, ReceiveDetailVO.NTHRECETAXMNY);
		}
		BillModel detailModel = getCardPanel().getBillModel(ReceiveDetailVO.TABCODE);
		String pk_corp = cardPanel.getCorp();
		int inv_tax_first_mode = InvParamTool.getTaxFirstMode(pk_corp);
		for (int row = 0; row < rowCount; row++) {
			UFDouble nthrecemny = (UFDouble) detailModel.getValueAt(row, ReceiveDetailVO.NTHRECEMNY);
			UFDouble nthrecetaxmny = (UFDouble) detailModel.getValueAt(row, ReceiveDetailVO.NTHRECETAXMNY);
			if(inv_tax_first_mode == 0){
				//含税优先,通过含税计算无税
				nthrecemny = JZTaxRateUtil.calcMny(SafeCompute.div(ntaxrate, new UFDouble(100)), nthrecetaxmny);
			}
			else{
				//无税优先,通过无税计算含税
				nthrecetaxmny = JZTaxRateUtil.calcTaxMny(SafeCompute.div(ntaxrate, new UFDouble(100)), nthrecemny);
			}
			getCardPanel().setBodyValueAt(SafeCompute.sub(nthrecetaxmny, nthrecemny), row, ReceiveDetailVO.NTAXMNY);
			getCardPanel().setBodyValueAt(nthrecemny, row, ReceiveDetailVO.NTHRECEMNY);
			getCardPanel().setBodyValueAt(nthrecetaxmny, row, ReceiveDetailVO.NTHRECETAXMNY);
			getCardPanel().setBodyValueAt(ntaxrate, row, ReceiveVO.NTAXRATE);
		}
	}
	/**
	 * 收票表体“税率”编辑后事件
	 */
	@Override
	public void cardBodyAfterEdit(BillEditEvent e) {
		if(ReceiveBVO.NTAXRATE.equals(e.getKey())){
			doBodyWhenEditBodyRate(e);
		}
	}
	private void doBodyWhenEditBodyRate(BillEditEvent e){
		BillCardPanel cardPanel = getClientUI().getBillCardPanel();
		String curTabcode = cardPanel.getCurrentBodyTableCode();//当前页签
		if(curTabcode.equals(ReceiveBVO.TABCODE)){
			String[] taxmnyFields = new String[]{ReceiveVO.NINVTAXMNY};
			String[] mnyFields = new String[]{ReceiveVO.NINVMNY};
			InvMnyTool.computeMnyByTaxrate(ReceiveVO.NTAXRATE, taxmnyFields, mnyFields, cardPanel, e);
		}
		else if(curTabcode.equals(ReceiveDetailVO.TABCODE)){
			String[] taxmnyFields = new String[]{ReceiveDetailVO.NTHRECETAXMNY};
			String[] mnyFields = new String[]{ReceiveDetailVO.NTHRECEMNY};
			InvMnyTool.computeMnyByTaxrate(ReceiveVO.NTAXRATE, taxmnyFields, mnyFields, cardPanel, e);
		}
	}
}