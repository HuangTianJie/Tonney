package com.gop;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.gop.asset.service.ConfigAssetService;
import com.gop.code.consts.CommonCodeConst;
import com.gop.domain.*;
import com.gop.domain.enums.LockStatus;
import com.gop.domain.enums.MenuLevel;
import com.gop.domain.enums.RoleStatus;
import com.gop.exception.AppException;
import com.gop.mode.vo.PageModel;
import com.gop.task.finance.InnerReportTask;
import com.gop.user.dto.*;
import com.gop.user.service.AdministractorService;
import com.gop.user.service.RoleManagerService;
import com.gop.user.service.UserService;

import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.ibatis.javassist.runtime.Inner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {GuorenManagerWebSpringbootApplication.class})
@Slf4j
public class ServerTest {

  @Autowired
  private UserService userService;

  @Autowired
  private AdministractorService administractorService;

  @Autowired
  private RoleManagerService roleManagerService;

  @Autowired
  private InnerReportTask task;
  @Autowired
  private ConfigAssetService configAssetService;

  @Test
  public void testRoleList() throws ParseException {
    Integer pageNo = 0;
    Integer pageSize = 10;
    for (int i=1;i<10;i++) {
//      PageInfo<Role> pageInfo = roleManagerService.getRoleList(RoleStatus.ENABLE.getStatus(), i++, pageSize);
      PageInfo<ConfigAsset> pageInfo = configAssetService.getAvailableAssetCode(null,i,pageSize);
//      PageModel<RoleDto> pageMode = new PageModel<>();
//      pageMode.setPageNo(pageNo);
//      pageMode.setPageSize(pageSize);
//      pageMode.setPageNum(pageInfo.getPages());
//      pageMode.setTotal(pageInfo.getTotal());
//
//      List<RoleDto> lstDtos = pageInfo.getList().stream().map(role -> new RoleDto(role,
//                                                                                  roleManagerService
//                                                                                      .getAdminRoleByRoleId(
//                                                                                          role.getRoleId()).size()
//      )).collect(Collectors.toList());
//      pageMode.setList(lstDtos);
      for (ConfigAsset asset:pageInfo.getList()){
        System.out.println(asset.getAssetCode());
      }
      System.out.println("==== "+i+" ====");
      System.out.println("==== "+pageInfo.getPages()+" "+pageInfo.getTotal()+" ====");
    }
  }

  @Test
  public void testAdminRoleList() throws ParseException {
    Integer pageNo = 1;
    Integer pageSize = 10;
    Integer roleId = 1;
    PageInfo<AdminRole> pageInfo = roleManagerService.getAdminRoleList(roleId, pageNo, pageSize);
    PageModel<AdminRoleDto> pageMode = new PageModel<>();
    pageMode.setPageNo(pageNo);
    pageMode.setPageSize(pageSize);
    pageMode.setPageNum(pageInfo.getPages());
    pageMode.setTotal(pageInfo.getTotal());

    List<AdminRoleDto> lstDtos = pageInfo.getList().stream().map(adminRole -> new AdminRoleDto(
        adminRole,
        administractorService.getAdministractor(adminRole.getAdminId()).getOpName(),
        roleManagerService.getRoleById(adminRole.getRoleId()).getRoleName()
    )).collect(Collectors.toList());

    pageMode.setList(lstDtos);
    System.out.print("");
  }

  @Test
  public void testCreateRole() throws ParseException {
    int role = roleManagerService.getRoleByName("会计99");
    if (role > 0) {
      throw new AppException(CommonCodeConst.ROLE_EXIST);
    }
    roleManagerService.createRole("会计99");

    System.out.print("");
  }

  @Test
  public void testMenuRoleList() throws ParseException {
    Integer roleId = 39;
    List<Menu> pageInfo = roleManagerService.getMenuByParentId(MenuLevel.LEVEL0.getLevel(), Integer.parseInt("0"));

    List<MenuRole> menuRoles = roleManagerService.getMenuRoleList(roleId, RoleStatus.ENABLE.getStatus());
    List<MenuRoleDto> lstDtos = pageInfo.stream().map(menu -> new MenuRoleDto(
        menu,
        roleManagerService.getMenuByParentId(MenuLevel.LEVEL1.getLevel(), menu.getMenuId())
                          .stream().map(childMenu -> new MenuRoleDto(
            childMenu,
            null,
            (menuRoles == null || menuRoles.size() == 0) ? false : isChoose(menuRoles, childMenu.getMenuId())
        )).collect(Collectors.toList()),

        (menuRoles == null || menuRoles.size() == 0) ? false : isChoose(menuRoles, menu.getMenuId())
    )).collect(Collectors.toList());

    System.out.print("");
  }

  private boolean isChoose(List<MenuRole> menuRoles, Integer menuId) {
    for (MenuRole menuRole : menuRoles) {
      if (menuRole.getMenuId().equals(menuId)) {
        return true;
      }
    }
    return false;
  }

  @Test
  public void testUpdateMenuRole() throws ParseException {
    String str =
        "{\"roleId\":\"16\",\"menuAddRoleDtos\":[{\"menuId\":\"100\",\"hasChoose\":true},{\"menuId\":\"200\",\"hasChoose\":true},{\"menuId\":\"300\",\"hasChoose\":true},{\"menuId\":\"400\",\"hasChoose\":true},{\"menuId\":\"500\",\"hasChoose\":true},{\"menuId\":\"600\",\"hasChoose\":true},{\"menuId\":\"700\",\"hasChoose\":true},{\"menuId\":\"800\",\"hasChoose\":true},{\"menuId\":\"1001\",\"hasChoose\":true},{\"menuId\":\"1002\",\"hasChoose\":true},{\"menuId\":\"1003\",\"hasChoose\":true},{\"menuId\":\"2001\",\"hasChoose\":true},{\"menuId\":\"2002\",\"hasChoose\":true},{\"menuId\":\"3001\",\"hasChoose\":true},{\"menuId\":\"3002\",\"hasChoose\":true},{\"menuId\":\"3003\",\"hasChoose\":true},{\"menuId\":\"3004\",\"hasChoose\":true},{\"menuId\":\"3005\",\"hasChoose\":true},{\"menuId\":\"3006\",\"hasChoose\":true},{\"menuId\":\"4001\",\"hasChoose\":true},{\"menuId\":\"4002\",\"hasChoose\":true},{\"menuId\":\"4003\",\"hasChoose\":true},{\"menuId\":\"5001\",\"hasChoose\":true},{\"menuId\":\"6001\",\"hasChoose\":true},{\"menuId\":\"6002\",\"hasChoose\":true},{\"menuId\":\"6003\",\"hasChoose\":true},{\"menuId\":\"6004\",\"hasChoose\":true},{\"menuId\":\"6005\",\"hasChoose\":true},{\"menuId\":\"7001\",\"hasChoose\":true},{\"menuId\":\"7002\",\"hasChoose\":true},{\"menuId\":\"7003\",\"hasChoose\":true},{\"menuId\":\"8001\",\"hasChoose\":true},{\"menuId\":\"8002\",\"hasChoose\":true}],\"brokerId\":\"10003\"}";
    JSONArray meu = JSON.parseObject(str).getJSONArray("menuAddRoleDtos");
    Integer roleId = JSON.parseObject(str).getInteger("roleId");
    List<MenuAddRoleDto> menuAddRoleDtoList = meu.stream().map(menu -> {
      JSONObject menuArray = (JSONObject) menu;
      return new MenuAddRoleDto(menuArray.getInteger("menuId"), menuArray.getBoolean("hasChoose"));
    }).collect(Collectors.toList());

    if (roleId == null || menuAddRoleDtoList == null || menuAddRoleDtoList.size() == 0) {
      throw new AppException(CommonCodeConst.FIELD_ERROR);
    }
//    List<MenuAddRoleDto> menuAddRoleDtoList = meu.toJavaList(MenuAddRoleDto.class);//JSONObject.parseArray(meu,MenuAddRoleDto.class);
//    Integer roleId = 10;
//    List<MenuAddRoleDto> menuAddRoleDtos = new ArrayList<>();
//    menuAddRoleDtos.add(new MenuAddRoleDto(1001,false));
//    menuAddRoleDtos.add(new MenuAddRoleDto(1003,true));
//    menuAddRoleDtos.add(new MenuAddRoleDto(3004,true));

    for (MenuAddRoleDto menuAddRoleDto : menuAddRoleDtoList) {
      int count = roleManagerService.getMenuRole(roleId, menuAddRoleDto.getMenuId());
      if (menuAddRoleDto.isHasChoose()) {
        if (count > 0) {
          roleManagerService.updateMenuRole(roleId, menuAddRoleDto.getMenuId(), RoleStatus.ENABLE.getStatus());
        } else {
          roleManagerService.addMenuRole(roleId, menuAddRoleDto.getMenuId(), RoleStatus.ENABLE.getStatus());
        }
      } else {
        if (count > 0) {
          roleManagerService.updateMenuRole(roleId, menuAddRoleDto.getMenuId(), RoleStatus.DISABLE.getStatus());
        }
      }

    }
    System.out.print("");
  }

  @Test
  public void testDeleteRole() throws ParseException {
    Integer roleId = 41;
    roleManagerService.deleteRole(roleId);
    System.out.print("");
  }

  @Test
  public void testAddAdminRole() throws ParseException {
    Integer roleId = 10;
    String account = "15210018354";
    Administrators admin = administractorService.getAdministractor(account);
    if (admin == null) {
      throw new AppException(CommonCodeConst.INVALID_REQUEST);
    }
    roleManagerService.createAdminRole(admin.getAdminId(), roleId);
  }

  @Test
  public void testCreateAdmin() throws ParseException {
    CreateAdministratorDto dto = new CreateAdministratorDto();
    dto.setRoleId(1);
    dto.setAccount("1521111");
    dto.setPassword("123456");
    dto.setUserName("ceshi12");
    Integer adminId = 1;
    log.info("userName is : " + dto.getUserName());
    administractorService.createAdminstrator(new Administrators(), adminId, dto.getAccount(), dto.getPassword(), dto.getUserName(),
                                             dto.getRoleId(), null);
    System.out.print("");
  }

  @Test
  public void TestWalletReport() {
//    task.platAssetStatus();
//    task.platAssetProess();
//    task.walletBalanceDailyReport();
//    task.totalDailyReport();
//    task.sendEmail();
//    task.walletBalanceDailyReport();
//    ShardingContext a=null;
//    task.execute(a);
    final String fileDate = DateFormatUtils.format(Calendar.getInstance().getTime(), "yyyyMMdd");
    System.out.println(fileDate);
  }
}