/*
 * This file is part of bisq.
 *
 * bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package io.bisq.gui.components.paymentmethods;

import io.bisq.gui.components.InputTextField;
import io.bisq.gui.util.BSFormatter;
import io.bisq.gui.util.Layout;
import io.bisq.gui.util.validation.AccountNrValidator;
import io.bisq.gui.util.validation.BranchIdValidator;
import io.bisq.gui.util.validation.InputValidator;
import io.bisq.locale.Res;
import io.bisq.locale.TradeCurrency;
import io.bisq.payload.payment.FasterPaymentsAccountContractData;
import io.bisq.payload.payment.PaymentAccountContractData;
import io.bisq.payment.FasterPaymentsAccount;
import io.bisq.payment.PaymentAccount;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.bisq.gui.util.FormBuilder.addLabelInputTextField;
import static io.bisq.gui.util.FormBuilder.addLabelTextField;

public class FasterPaymentsForm extends PaymentMethodForm {
    private static final Logger log = LoggerFactory.getLogger(FasterPaymentsForm.class);

    private final FasterPaymentsAccount fasterPaymentsAccount;
    private InputTextField accountNrInputTextField;
    private InputTextField sortCodeInputTextField;

    public static int addFormForBuyer(GridPane gridPane, int gridRow,
                                      PaymentAccountContractData paymentAccountContractData) {
        // do not translate as it is used in english only
        addLabelTextField(gridPane, ++gridRow, "UK sort code:",
                ((FasterPaymentsAccountContractData) paymentAccountContractData).getSortCode());
        addLabelTextField(gridPane, ++gridRow, Res.get("payment.accountNr"),
                ((FasterPaymentsAccountContractData) paymentAccountContractData).getAccountNr());
        return gridRow;
    }

    public FasterPaymentsForm(PaymentAccount paymentAccount, InputValidator inputValidator, GridPane gridPane,
                              int gridRow, BSFormatter formatter) {
        super(paymentAccount, inputValidator, gridPane, gridRow, formatter);
        this.fasterPaymentsAccount = (FasterPaymentsAccount) paymentAccount;
    }

    @Override
    public void addFormForAddAccount() {
        gridRowFrom = gridRow + 1;
        // do not translate as it is used in english only
        sortCodeInputTextField = addLabelInputTextField(gridPane, ++gridRow, "UK sort code:").second;
        sortCodeInputTextField.setValidator(inputValidator);
        sortCodeInputTextField.setValidator(new BranchIdValidator("GB"));
        sortCodeInputTextField.textProperty().addListener((ov, oldValue, newValue) -> {
            fasterPaymentsAccount.setSortCode(newValue);
            updateFromInputs();
        });

        accountNrInputTextField = addLabelInputTextField(gridPane, ++gridRow, Res.get("payment.accountNr")).second;
        accountNrInputTextField.setValidator(new AccountNrValidator("GB"));
        accountNrInputTextField.textProperty().addListener((ov, oldValue, newValue) -> {
            fasterPaymentsAccount.setAccountNr(newValue);
            updateFromInputs();
        });

        TradeCurrency singleTradeCurrency = fasterPaymentsAccount.getSingleTradeCurrency();
        String nameAndCode = singleTradeCurrency != null ? singleTradeCurrency.getNameAndCode() : "";
        addLabelTextField(gridPane, ++gridRow, Res.getWithCol("shared.currency"),
                nameAndCode);
        addAllowedPeriod();
        addAccountNameTextFieldWithAutoFillCheckBox();
    }

    @Override
    protected void autoFillNameTextField() {
        if (useCustomAccountNameCheckBox != null && !useCustomAccountNameCheckBox.isSelected()) {
            String accountNr = accountNrInputTextField.getText();
            accountNr = StringUtils.abbreviate(accountNr, 9);
            String method = Res.get(paymentAccount.getPaymentMethod().getId());
            accountNameTextField.setText(method.concat(": ").concat(accountNr));
        }
    }

    @Override
    public void addFormForDisplayAccount() {
        gridRowFrom = gridRow;
        addLabelTextField(gridPane, gridRow, Res.get("payment.account.name"),
                fasterPaymentsAccount.getAccountName(), Layout.FIRST_ROW_AND_GROUP_DISTANCE);
        addLabelTextField(gridPane, ++gridRow, Res.getWithCol("shared.paymentMethod"),
                Res.get(fasterPaymentsAccount.getPaymentMethod().getId()));
        // do not translate as it is used in english only
        addLabelTextField(gridPane, ++gridRow, "UK sort code:", fasterPaymentsAccount.getSortCode());
        TextField field = addLabelTextField(gridPane, ++gridRow, Res.get("payment.accountNr"),
                fasterPaymentsAccount.getAccountNr()).second;
        field.setMouseTransparent(false);
        TradeCurrency singleTradeCurrency = fasterPaymentsAccount.getSingleTradeCurrency();
        String nameAndCode = singleTradeCurrency != null ? singleTradeCurrency.getNameAndCode() : "";
        addLabelTextField(gridPane, ++gridRow, Res.getWithCol("shared.currency"), nameAndCode);
        addAllowedPeriod();
    }

    @Override
    public void updateAllInputsValid() {
        allInputsValid.set(isAccountNameValid()
                && sortCodeInputTextField.getValidator().validate(fasterPaymentsAccount.getSortCode()).isValid
                && accountNrInputTextField.getValidator().validate(fasterPaymentsAccount.getAccountNr()).isValid
                && fasterPaymentsAccount.getTradeCurrencies().size() > 0);
    }
}
