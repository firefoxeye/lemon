package com.mossle.user.web;

import java.awt.image.BufferedImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import javax.imageio.ImageIO;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.mossle.api.internal.StoreConnector;
import com.mossle.api.internal.StoreDTO;
import com.mossle.api.scope.ScopeHolder;
import com.mossle.api.user.UserCache;
import com.mossle.api.user.UserDTO;

import com.mossle.core.hibernate.PropertyFilter;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.page.Page;
import com.mossle.core.spring.MessageHelper;
import com.mossle.core.util.IoUtils;
import com.mossle.core.util.ServletUtils;

import com.mossle.ext.export.Exportor;
import com.mossle.ext.export.TableModel;
import com.mossle.ext.store.InputStreamDataSource;
import com.mossle.ext.store.MultipartFileDataSource;

import com.mossle.user.ImageUtils;
import com.mossle.user.persistence.domain.AccountAvatar;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.manager.AccountAvatarManager;
import com.mossle.user.persistence.manager.AccountInfoManager;

import org.springframework.core.io.InputStreamResource;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("user")
public class AccountAvatarController {
    private AccountInfoManager accountInfoManager;
    private AccountAvatarManager accountAvatarManager;
    private MessageHelper messageHelper;
    private Exportor exportor;
    private BeanMapper beanMapper = new BeanMapper();
    private StoreConnector storeConnector;

    @RequestMapping("account-avatar-input")
    public String input(@RequestParam("id") Long id, Model model) {
        AccountInfo accountInfo = accountInfoManager.get(id);
        String hql = "from AccountAvatar where accountInfo=? and type='default'";
        AccountAvatar accountAvatar = accountAvatarManager.findUnique(hql,
                accountInfo);
        model.addAttribute("accountInfo", accountInfo);
        model.addAttribute("accountAvatar", accountAvatar);

        return "user/account-avatar-input";
    }

    /**
     * 上传.
     */
    @RequestMapping("account-avatar-upload")
    @ResponseBody
    public String upload(@RequestParam("id") Long id,
            @RequestParam("avatar") MultipartFile avatar) throws Exception {
        StoreDTO storeDto = storeConnector.saveStore("avatar",
                new MultipartFileDataSource(avatar));

        AccountInfo accountInfo = accountInfoManager.get(id);
        String hql = "from AccountAvatar where accountInfo=? and type='default'";
        AccountAvatar accountAvatar = accountAvatarManager.findUnique(hql,
                accountInfo);

        if (accountAvatar == null) {
            accountAvatar = new AccountAvatar();
            accountAvatar.setAccountInfo(accountInfo);
            accountAvatar.setType("default");
        }

        accountAvatar.setCode(storeDto.getKey());
        accountAvatarManager.save(accountAvatar);

        return "{\"success\":true,\"id\":\"" + id + "\"}";
    }

    /**
     * 显示.
     */
    @RequestMapping("account-avatar-view")
    @ResponseBody
    public void avatar(@RequestParam("id") Long id, OutputStream os)
            throws Exception {
        AccountInfo accountInfo = accountInfoManager.get(id);
        String hql = "from AccountAvatar where accountInfo=? and type='default'";
        AccountAvatar accountAvatar = accountAvatarManager.findUnique(hql,
                accountInfo);

        if (accountAvatar == null) {
            return;
        }

        StoreDTO storeDto = storeConnector.getStore("avatar",
                accountAvatar.getCode());

        IoUtils.copyStream(storeDto.getDataSource().getInputStream(), os);
    }

    @RequestMapping("account-avatar-crop")
    public String crop(@RequestParam("id") Long id, Model model)
            throws Exception {
        AccountInfo accountInfo = accountInfoManager.get(id);
        String hql = "from AccountAvatar where accountInfo=? and type='default'";
        AccountAvatar accountAvatar = accountAvatarManager.findUnique(hql,
                accountInfo);
        model.addAttribute("accountInfo", accountInfo);
        model.addAttribute("accountAvatar", accountAvatar);

        if (accountAvatar == null) {
            return "user/account-avatar-crop";
        }

        StoreDTO storeDto = storeConnector.getStore("avatar",
                accountAvatar.getCode());
        BufferedImage bufferedImage = ImageIO.read(storeDto.getDataSource()
                .getInputStream());
        int height = bufferedImage.getHeight();
        int width = bufferedImage.getWidth();

        if (height > width) {
            int h = 512;
            int w = (512 * width) / height;
            int min = w;
            model.addAttribute("h", h);
            model.addAttribute("w", w);
            model.addAttribute("min", min);
        } else {
            int w = 512;
            int h = (512 * height) / width;
            int min = h;
            model.addAttribute("h", h);
            model.addAttribute("w", w);
            model.addAttribute("min", min);
        }

        return "user/account-avatar-crop";
    }

    @RequestMapping("account-avatar-save")
    public String save(@RequestParam("id") Long id, @RequestParam("x1") int x1,
            @RequestParam("x2") int x2, @RequestParam("y1") int y1,
            @RequestParam("y2") int y2, @RequestParam("w") int w, Model model)
            throws Exception {
        AccountInfo accountInfo = accountInfoManager.get(id);
        String hql = "from AccountAvatar where accountInfo=? and type='default'";
        AccountAvatar accountAvatar = accountAvatarManager.findUnique(hql,
                accountInfo);
        model.addAttribute("accountInfo", accountInfo);
        model.addAttribute("accountAvatar", accountAvatar);

        if (accountAvatar != null) {
            StoreDTO storeDto = storeConnector.getStore("avatar",
                    accountAvatar.getCode());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageUtils.zoomImage(storeDto.getDataSource().getInputStream(),
                    baos, x1, y1, x2, y2);

            storeDto = storeConnector.saveStore("avatar",
                    new InputStreamDataSource(w + ".png",
                            new ByteArrayInputStream(baos.toByteArray())));
            accountAvatar.setCode(storeDto.getKey());
            accountAvatarManager.save(accountAvatar);
        }

        return "user/account-avatar-save";
    }

    // ~ ======================================================================
    @Resource
    public void setAccountInfoManager(AccountInfoManager accountInfoManager) {
        this.accountInfoManager = accountInfoManager;
    }

    @Resource
    public void setAccountAvatarManager(
            AccountAvatarManager accountAvatarManager) {
        this.accountAvatarManager = accountAvatarManager;
    }

    @Resource
    public void setMessageHelper(MessageHelper messageHelper) {
        this.messageHelper = messageHelper;
    }

    @Resource
    public void setExportor(Exportor exportor) {
        this.exportor = exportor;
    }

    @Resource
    public void setStoreConnector(StoreConnector storeConnector) {
        this.storeConnector = storeConnector;
    }
}
