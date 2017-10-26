package nc.ui.jzinv.receive;

import java.util.HashMap;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.jzinv.invpub.IJzinvQuery;
import nc.ui.jzinv.pub.button.BillLinkBtnVO;
import nc.ui.jzinv.pub.button.LinkQueryPfBtnVO;
import nc.ui.jzinv.pub.buttonstate.RedInvBtnVO;
import nc.ui.jzinv.pub.buttonstate.RefAddCollBtnVO;
import nc.ui.jzinv.pub.tool.DeptPsnRela;
import nc.ui.jzinv.pub.ui.MultiChildBillManageUI;
import nc.ui.jzinv.receive.handler.BisredEditHandler;
import nc.ui.jzinv.receive.handler.BissplitEditHandler;
import nc.ui.jzinv.receive.handler.BlueInvoiceEditHandler;
import nc.ui.jzinv.receive.handler.ContractEditHandler;
import nc.ui.jzinv.receive.handler.NinvmnyEditHandler;
import nc.ui.jzinv.receive.handler.NnumEditHandler;
import nc.ui.jzinv.receive.handler.NpriceEditHandler;
import nc.ui.jzinv.receive.handler.NtaxmnyEditHandler;
import nc.ui.jzinv.receive.handler.NtaxrateEditHandler;
import nc.ui.jzinv.receive.handler.NthrecemnyEditHandler;
import nc.ui.jzinv.receive.handler.NtotalinvoicetaxAfterEdit;
import nc.ui.jzinv.receive.handler.OpenDateEditHandler;
import nc.ui.jzinv.receive.handler.ProjectEditHandler;
import nc.ui.jzinv.receive.handler.SupplierEditHandler;
import nc.ui.jzinv.receive.handler.VinvcodeEditHandler;
import nc.ui.jzinv.receive.handler.VinvnoEditHandler;
import nc.ui.pub.bill.BillCardBeforeEditListener;
import nc.ui.pub.bill.BillCardPanel;
import nc.ui.pub.bill.BillEditEvent;
import nc.ui.pub.bill.BillItem;
import nc.ui.pub.bill.BillItemEvent;
import nc.ui.trade.bill.AbstractManageController;
import nc.ui.trade.bsdelegate.BusinessDelegator;
import nc.ui.trade.buttonstate.RefBillBtnVO;
import nc.ui.trade.manage.ManageEventHandler;
import nc.vo.jzinv.pub.IJzinvBillType;
import nc.vo.jzinv.pub.IJzinvButton;
import nc.vo.jzinv.pub.ParamReader;
import nc.vo.jzinv.receive.ReceiveBVO;
import nc.vo.jzinv.receive.ReceiveDetailVO;
import nc.vo.jzinv.receive.ReceiveVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.lang.UFDouble;
import nc.vo.trade.button.ButtonVO;

/**
 * 收票UI
 * @author mayyc
 *
 */
public class ReceiveUI extends MultiChildBillManageUI implements ChangeListener, BillCardBeforeEditListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 扩展按钮状态--可用
	 */
	public final static int ENABLE = 1;

	public ReceiveUI() {
		super();
	}

	@Override
	protected AbstractManageController createController() {
		return new ReceiveCtrl();
	}

	protected BusinessDelegator createBusinessDelegator() {
		return new ReceiveDelegator();
	}

	@Override
	protected ManageEventHandler createEventHandler() {
		return new ReceiveEH(this, getUIControl());
	}

	@Override
	protected ButtonVO[] initAssQryBtnVOs() {
		//添加单据联查按钮
		BillLinkBtnVO billLinkBtnVo = new BillLinkBtnVO();

		return new ButtonVO[] { new LinkQueryPfBtnVO().getButtonVO(), billLinkBtnVo.getButtonVO() };
	}

	@Override
	public void afterEdit(BillEditEvent e) {
		super.afterEdit(e);
		if (e.getPos() == 1) {
			afterBodyEdit(e);
		} else {
			afterHeadEdit(e);
		}
	}

	@Override
	protected void initPrivateButton() {
		super.initPrivateButton();
		addRefBtn();
		addRedBtn();
	}

	/**
	 * 参照按钮
	 */
	private void addRefBtn() {
		RefAddCollBtnVO refAddBtn = new RefAddCollBtnVO();
		refAddBtn.getButtonVO().setExtendStatus(new int[] { ENABLE });
		addPrivateButton(refAddBtn.getButtonVO());
		ButtonVO refBtnVO = new RefBillBtnVO().getButtonVO();
		refBtnVO.setChildAry(new int[] { IJzinvButton.REFADDCOLLBTN });
		addPrivateButton(refBtnVO);
	}

	/**
	 * 红票按钮
	 */
	private void addRedBtn() {
		RedInvBtnVO redBtn = new RedInvBtnVO();
		redBtn.getButtonVO().setExtendStatus(new int[] { ENABLE });
		addPrivateButton(redBtn.getButtonVO());
	}

	/**
	 * 表头编辑后事件
	 * @param e
	 */
	public void afterHeadEdit(BillEditEvent e) {
		if (e.getKey().equals("pk_deptdoc")) {// 经办部门	
			getDeptPsnRela().setPsnByDept();
			//			getBillCardPanel().execHeadEditFormulas();
		} else if (e.getKey().equals("pk_psndoc")) {// 经办人
			getDeptPsnRela().setDeptByPsn();
			//			getBillCardPanel().execHeadEditFormulas();
		} else if (ReceiveVO.BISRED.equals(e.getKey())) {
			//getBillCardPanel().execHeadEditFormulas();
		} else if (ReceiveVO.BISRED.equals(e.getKey())) {
			new BisredEditHandler(this).cardHeadAfterEdit(e);
		} else if (ReceiveVO.PK_RECEIVE_REF.equals(e.getKey())) {
			new BlueInvoiceEditHandler(this).cardHeadAfterEdit(e);
		} else if (ReceiveVO.PK_PROJECT.equals(e.getKey())) {
			new ProjectEditHandler(this).cardHeadAfterEdit(e);
		} else if (ReceiveVO.PK_SUPPLIER.equals(e.getKey())) {
			new SupplierEditHandler(this).cardHeadAfterEdit(e);
			String pk_supplierbase = (String) getBillCardPanel().getHeadItem(ReceiveVO.PK_SUPPLIERBASE)
					.getValueObject();
			IJzinvQuery service = NCLocator.getInstance().lookup(IJzinvQuery.class);
			String str = "";
			try {
				str = service.getAccountAndAccountNum(pk_supplierbase);
			} catch (BusinessException e1) {
				Logger.error(e1.getMessage(), e1);
			}
			//vsupbankaccount	销售方开户银行及银行账号
			getBillCardPanel().setHeadItem(ReceiveVO.VSUPBANKACCOUNT, str);
		} else if (ReceiveVO.NINVMNY.equals(e.getKey()) || ReceiveVO.NINVTAXMNY.equals(e.getKey())) {
			new NinvmnyEditHandler(this).cardHeadAfterEdit(e);
		} else if (ReceiveVO.NTAXRATE.equals(e.getKey())) {
			new NtaxrateEditHandler(this).cardHeadAfterEdit(e);
		} else if (ReceiveVO.NTAXMNY.equals(e.getKey())) {
			new NtaxmnyEditHandler(this).cardHeadAfterEdit(e);
		} else if (ReceiveVO.PK_CONTRACT.equals(e.getKey())) {
			new ContractEditHandler(this).cardHeadAfterEdit(e);
		} else if (ReceiveVO.DOPENDATE.equals(e.getKey())) {
			new OpenDateEditHandler(this).cardHeadAfterEdit(e);
		}
		//linan 增加代码 控制选中是否拆分后的逻辑
		else if (ReceiveVO.BISSPLIT.equals(e.getKey())) {
			new BissplitEditHandler(this).cardHeadAfterEdit(e);
		}
		else if (ReceiveVO.VINVCODE.equals(e.getKey())) {
			new VinvcodeEditHandler(this).cardHeadAfterEdit(e);
		}
		else if (ReceiveVO.VINVNO.equals(e.getKey())) {
			new VinvnoEditHandler(this).cardHeadAfterEdit(e);
		}
		//票面总税金
		else if (ReceiveVO.NTOTALINVOICETAX.equals(e.getKey())) {
			new NtotalinvoicetaxAfterEdit(this).cardHeadAfterEdit(e);
		}
	}

	/**
	 * 表体编辑后事件
	 * @param e
	 */
	public void afterBodyEdit(BillEditEvent e) {
		String currentTabCode = getBillCardPanel().getCurrentBodyTableCode();
		if (currentTabCode.equals(ReceiveBVO.TABCODE)) {
			if (ReceiveBVO.NINVMNY.equals(e.getKey()) || ReceiveBVO.NINVTAXMNY.equals(e.getKey())) {
				new NinvmnyEditHandler(this).cardBodyAfterEdit(e);
			} else if (ReceiveBVO.NTAXRATE.equals(e.getKey())) {
				new NtaxrateEditHandler(this).cardBodyAfterEdit(e);
			} else if (ReceiveBVO.NTAXMNY.equals(e.getKey())) {
				new NtaxmnyEditHandler(this).cardBodyAfterEdit(e);
			} else if (ReceiveBVO.NPRICEMNY.equals(e.getKey()) || ReceiveBVO.NPRICETAXMNY.equals(e.getKey())) {
				new NpriceEditHandler(this).cardBodyAfterEdit(e);
			} else if (ReceiveBVO.NNUM.equals(e.getKey())) {
				new NnumEditHandler(this).cardBodyAfterEdit(e);
			}
		} else if (currentTabCode.equals(ReceiveDetailVO.TABCODE)) {
			if (ReceiveDetailVO.NTHRECEMNY.equals(e.getKey()) || ReceiveDetailVO.NTHRECETAXMNY.equals(e.getKey())) {
				new NthrecemnyEditHandler(this).cardBodyAfterEdit(e);
			} else if (ReceiveDetailVO.VCONTRACTCODE.equals(e.getKey())) {
				new ContractEditHandler(this).cardBodyAfterEdit(e);
			} else if (ReceiveBVO.NTAXRATE.equals(e.getKey())) {
				new NtaxrateEditHandler(this).cardBodyAfterEdit(e);
			} else if (ReceiveDetailVO.NTAXMNY.equals(e.getKey())) {
				new NtaxmnyEditHandler(this).cardBodyAfterEdit(e);
			}
		}
	}

	@Override
	protected void initEventListener() {
		this.getBillCardPanel().setBillBeforeEditListenerHeadTail(this);
		super.initEventListener();
	}

	@Override
	public void setDefaultData() throws Exception {
		super.setDefaultData();
		getBillCardPanel().getHeadItem(ReceiveVO.PK_RECEIVE_REF).setEdit(false);
		getBillCardPanel().getHeadItem(ReceiveVO.PK_RECEIVE_REF).setNull(false);
		getBillCardPanel().getBillModel().execLoadFormula();
		getBillListPanel().getHeadBillModel().execLoadFormula();
		getDeptPsnRela().setOnAdd();
		// 原币 
		getBillCardPanel().setHeadItem(getOriginCurrFldName(), getCy().getLocalCurrPK()); //新增时赋值原币币种
		getBillCardPanel().setHeadItem(getBaseCurrFldName(), getCy().getLocalCurrPK()); //新增时赋值原币币种
		getBillCardPanel().setHeadItem(getBaseRateFldName(), new UFDouble(1.0));
	}

	public DeptPsnRela getDeptPsnRela() {
		DeptPsnRela deptPsnRela = null;
		BillCardPanel billCardPanel = getBillCardPanel();
		BillItem deptItem = billCardPanel.getHeadItem(ReceiveVO.PK_DEPTDOC);
		BillItem psnItem = billCardPanel.getHeadItem(ReceiveVO.PK_PSNDOC);
		deptPsnRela = new DeptPsnRela(deptItem, psnItem);
		return deptPsnRela;
	}

	/**
	 * 表头编辑前事件
	 */
	public boolean beforeEdit(BillItemEvent e) {
		String key = e.getItem().getKey();
		if (ReceiveVO.PK_CONTRACT.equals(key)) {
			new ContractEditHandler(this).cardHeadBeforeEdit(e);
		}
		
		return true;
	}

	/**
	 * 表体编辑前事件
	 */
	public boolean beforeEdit(BillEditEvent e) {
		return super.beforeEdit(e);
	}

	@Override
	public String[] getHeadOriginItems() {
		return new String[] { ReceiveVO.NORIGINVMNY, ReceiveVO.NORIGINVTAXMNY, ReceiveVO.NORIGBACKMNY,
				ReceiveVO.NORIGBACKTAXMNY, ReceiveVO.NORIGHADREDMNY, ReceiveVO.NORIGHADREDTAXMNY,
				ReceiveVO.NORIGHADSETLEVEFYMNY, ReceiveVO.NORIGHADSETLEVEFYTAXMNY, ReceiveVO.NORIGHADPAYVEFYMNY,
				ReceiveVO.NORIGHADPAYVEFYTAXMNY };
	}

	@Override
	public String[] getHeadBaseItems() {
		return new String[] { ReceiveVO.NINVMNY, ReceiveVO.NINVTAXMNY, ReceiveVO.NTAXMNY, ReceiveVO.NBACKMNY,
				ReceiveVO.NBACKTAXMNY, ReceiveVO.NHADREDMNY, ReceiveVO.NHADREDTAXMNY, ReceiveVO.NHADSETLEVEFYMNY,
				ReceiveVO.NHADSETLEVEFYTAXMNY, ReceiveVO.NHADPAYVEFYMNY, ReceiveVO.NHADPAYVEFYTAXMNY,
				ReceiveVO.NNOSETLEVEFYMNY, ReceiveVO.NNOSETLEVEFYTAXMNY, ReceiveVO.NNOPAYVEFYTAXMNY,
				ReceiveVO.NFHADDDUCTMNY,ReceiveVO.NFHADDDUCTTAXMNY,ReceiveVO.NTOTALINVOICEAMOUNTMNY,ReceiveVO.NTOTALINVOICEAMOUNTTAXMNY,
				ReceiveVO.NTOTALINVOICETAX,ReceiveVO.NCUMULATIVESPLITTAX,ReceiveVO.NSURPLUSSPLITTAX
		};
	}

	@Override
	public String[][] getBodyOriginItems() {
		return new String[][] {
				{ ReceiveBVO.NORIGINVMNY, ReceiveBVO.NORIGINVTAXMNY,ReceiveBVO.NTAXMNY },
				{ ReceiveDetailVO.NORIGCONTRAMNY, ReceiveDetailVO.NORIGCONTRATAXMNY, ReceiveDetailVO.NORIGSUMRECEMNY,
						ReceiveDetailVO.NORIGSUMRECETAXMNY, ReceiveDetailVO.NORIGTHRECEMNY,
						ReceiveDetailVO.NORIGTHRECETAXMNY, ReceiveDetailVO.NORIGAPPLIEDMNY,
						ReceiveDetailVO.NORIGAPPLIEDTAXMNY, ReceiveDetailVO.NORIGPAIDMNY,
						ReceiveDetailVO.NORIGPAIDTAXMNY, ReceiveDetailVO.NORIGPREPAYMNY,
						ReceiveDetailVO.NORIGPREPAYTAXMNY,ReceiveDetailVO.NTAXMNY } };
	}

	@Override
	public String[][] getBodyBaseItems() {
		return new String[][] {
				{ ReceiveBVO.NINVMNY, ReceiveBVO.NINVTAXMNY,ReceiveBVO.NTAXMNY },
				{ ReceiveDetailVO.NCONTRAMNY, ReceiveDetailVO.NCONTRATAXMNY, ReceiveDetailVO.NSUMRECEMNY,
						ReceiveDetailVO.NSUMRECETAXMNY, ReceiveDetailVO.NTHRECEMNY, ReceiveDetailVO.NTHRECETAXMNY,
						ReceiveDetailVO.NAPPLIEDMNY, ReceiveDetailVO.NAPPLIEDTAXMNY, ReceiveDetailVO.NPAIDMNY,
						ReceiveDetailVO.NPAIDTAXMNY, ReceiveDetailVO.NPREPAYMNY, ReceiveDetailVO.NPREPAYTAXMNY,ReceiveDetailVO.NTAXMNY } };
	}

	public String[] getTableCodes() {
		return new String[] { ReceiveBVO.TABCODE, ReceiveDetailVO.TABCODE };
	}

	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBodySpecialData(CircularlyAccessibleValueObject[] vos) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setHeadSpecialData(CircularlyAccessibleValueObject vo, int intRow) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setTotalHeadSpecialData(CircularlyAccessibleValueObject[] vos) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initSelfData() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getRefBillType() {
		return IJzinvBillType.JZINV_RECEIPT_COLL;
	}

	private HashMap<String, String[][]> bodyDigitMap = null;

	@Override
	/**
	 *表体单价数量特殊精度参数处理
	 */
	public HashMap<String, String[][]> getBodyOthDigitItems() {
		if (bodyDigitMap == null) {
			bodyDigitMap = new HashMap<String, String[][]>();
			bodyDigitMap.put(
					ReceiveBVO.TABCODE,
					new String[][] {
							{ ReceiveBVO.NNUM, ReceiveBVO.NPRICEMNY, ReceiveBVO.NPRICETAXMNY },
							{ ParamReader.getParamter(_getCorp().getPk_corp(), ParamReader.NUMBER_DIGITAL),
									ParamReader.getParamter(_getCorp().getPk_corp(), ParamReader.PRICE_DIGITAL),
									ParamReader.getParamter(_getCorp().getPk_corp(), ParamReader.PRICE_DIGITAL) } });
		}
		return bodyDigitMap;
	}

}
