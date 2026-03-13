/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package instagram;

import java.io.Serializable;

/**
 *
 * @author Nathan
 */
public abstract class AbstractAccount implements Serializable, Activable {
    private static final long serialVersionUID = 1L;

    protected String username;
    protected String password;
    protected boolean isActive;
    protected AccountType accountType;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public boolean isPrivateAccount() {
        return accountType == AccountType.PRIVATE;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void activate() {
        isActive = true;
    }

    @Override
    public void deactivate() {
        isActive = false;
    }
}
