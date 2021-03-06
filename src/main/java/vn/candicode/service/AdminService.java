package vn.candicode.service;

import vn.candicode.payload.request.NewAdminRequest;
import vn.candicode.payload.request.UpdateAdminRoleRequest;
import vn.candicode.security.UserPrincipal;

import java.io.IOException;

public interface AdminService {
    /**
     * @param payload
     * @param currentUser Only master admin can do this task
     * @return id of new admin
     */
    Long createAccount(NewAdminRequest payload, UserPrincipal currentUser) throws IOException;

    /**
     * @param payload
     * @param currentUser Only master admin can do this task
     */
    void updateRole(Long adminId, UpdateAdminRoleRequest payload, UserPrincipal currentUser);
}
