<?xml version="1.0"?>
<?mso-application progid="Excel.Sheet"?>
<Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet"
 xmlns:o="urn:schemas-microsoft-com:office:office"
 xmlns:x="urn:schemas-microsoft-com:office:excel"
 xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet"
 xmlns:html="http://www.w3.org/TR/REC-html40">
 
 <DocumentProperties xmlns="urn:schemas-microsoft-com:office:office">
	  <Author>administer</Author>
	  <LastAuthor>administer</LastAuthor>
	  <Created>2016-10-10T03:10:02Z</Created>
	  <Version>15.00</Version>
 </DocumentProperties>
 <OfficeDocumentSettings xmlns="urn:schemas-microsoft-com:office:office">
  <AllowPNG/>
 </OfficeDocumentSettings>
 <ExcelWorkbook xmlns="urn:schemas-microsoft-com:office:excel">
	  <WindowHeight>9384</WindowHeight>
	  <WindowWidth>23040</WindowWidth>
	  <WindowTopX>0</WindowTopX>
	  <WindowTopY>0</WindowTopY>
	  <ProtectStructure>False</ProtectStructure>
	  <ProtectWindows>False</ProtectWindows>
 </ExcelWorkbook>
 <Styles>
	  <Style ss:ID="Default" ss:Name="Normal">
	   <Alignment ss:Vertical="Center"/>
	   <Borders/>
	   <Font ss:FontName="宋体" x:CharSet="134" ss:Size="11" ss:Color="#000000"/>
	   <Interior/>
	   <NumberFormat/>
	   <Protection/>
	  </Style>
 </Styles>
 <#setting time_zone = "UTC/GMT+10:00">
 <Worksheet ss:Name="Sheet1">
  <Table x:FullColumns="1" x:FullRows="1" ss:DefaultRowHeight="14.4">
   <Column ss:AutoFitWidth="0" ss:Width="80.400000000000006"/>
   <Column ss:Index="3" ss:AutoFitWidth="0" ss:Width="69.599999999999994"/>
   <Column ss:Index="5" ss:AutoFitWidth="0" ss:Width="79.800000000000011"/>
   <Row>
    <Cell><Data ss:Type="String">挂单时间(UTC+0)</Data></Cell>
    <Cell><Data ss:Type="String">成交时间(UTC+0）</Data></Cell>
    <Cell><Data ss:Type="String">交易对</Data></Cell>
    <Cell><Data ss:Type="String">下单类型</Data></Cell>
    <Cell><Data ss:Type="String">下单种类</Data></Cell>
    <Cell><Data ss:Type="String">下单流水号</Data></Cell>
    <Cell><Data ss:Type="String">用户ID</Data></Cell>
    <Cell><Data ss:Type="String">下单数量</Data></Cell>
    <Cell><Data ss:Type="String">下单价格</Data></Cell>
    <Cell><Data ss:Type="String">已撮合数量</Data></Cell>
    <Cell><Data ss:Type="String">剩余未撮合数量</Data></Cell>
    <Cell><Data ss:Type="String">已撮合金额</Data></Cell>
    <Cell><Data ss:Type="String">交易手续费</Data></Cell>
    <Cell><Data ss:Type="String">订单状态</Data></Cell>
   </Row>
   <#list resultList as downloadMap>
      <Row>
        <Cell><Data ss:Type="String">${(downloadMap.createDate?string('yyyy-MM-dd HH:mm:ss'))!}</Data></Cell>
        <Cell><Data ss:Type="String">${(downloadMap.finishDate?string('yyyy-MM-dd HH:mm:ss'))!}</Data></Cell>
	    <Cell><Data ss:Type="String">${downloadMap.symbol}</Data></Cell>
	    <Cell><Data ss:Type="String">${downloadMap.tradeType}</Data></Cell>
	    <Cell><Data ss:Type="String">${downloadMap.tradeFlag}</Data></Cell>
	    <Cell><Data ss:Type="String">${downloadMap.requestNo}</Data></Cell>
        <Cell><Data ss:Type="String">${downloadMap.uid}</Data></Cell>
        <Cell><Data ss:Type="String">${downloadMap.number?string('0.00000000')!''}</Data></Cell>
	    <Cell><Data ss:Type="String">${downloadMap.price?string('0.00000000')!''}</Data></Cell>
	    <Cell><Data ss:Type="String">${downloadMap.tradedNumber?string('0.00000000')!''}</Data></Cell>
	    <Cell><Data ss:Type="String">${downloadMap.numberOver?string('0.00000000')!''}</Data></Cell>
	    <Cell><Data ss:Type="String">${downloadMap.tradedMoney?string('0.00000000')!''}</Data></Cell>
	    <Cell><Data ss:Type="String">${downloadMap.fee?string('0.00000000')!''}</Data></Cell>
	    <Cell><Data ss:Type="String">${downloadMap.status}</Data></Cell>
      </Row>
   </#list>
  </Table>
  <WorksheetOptions xmlns="urn:schemas-microsoft-com:office:excel">
   <PageSetup>
    <Header x:Margin="0.3"/>
    <Footer x:Margin="0.3"/>
    <PageMargins x:Bottom="0.75" x:Left="0.7" x:Right="0.7" x:Top="0.75"/>
   </PageSetup>
   <Selected/>
   <Panes>
    <Pane>
     <Number>3</Number>
     <ActiveRow>8</ActiveRow>
     <ActiveCol>6</ActiveCol>
    </Pane>
   </Panes>
   <ProtectObjects>False</ProtectObjects>
   <ProtectScenarios>False</ProtectScenarios>
  </WorksheetOptions>
 </Worksheet>
</Workbook>