package org.mapfish.print.output;

import hr.yottabyte.digmap.billing.CostAndData;
import hr.yottabyte.digmap.billing.CostCalc;
import hr.yottabyte.digmap.config.DigMapConfig;
import hr.yottabyte.digmap.signer.PdfAttacher;
import hr.yottabyte.digmap.signer.PdfSigner;
import hr.yottabyte.digmap.v2.wfs.WfsUtils;
import hr.yottabyte.digmap.v2.wfs.WfsLayersConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.mapfish.print.Constants;
import org.mapfish.print.RenderingContext;
import org.mapfish.print.config.layout.Layout;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfStream;
import com.itextpdf.text.pdf.PdfWriter;

public abstract class AbstractOutputFormat implements OutputFormat {
	
    private static final Logger LOGGER = Logger.getLogger(AbstractOutputFormat.class);	

    protected RenderingContext doPrint(PrintParams params) throws DocumentException {
        final String layoutName = params.jsonSpec.getString(Constants.JSON_LAYOUT_KEY);
        Layout layout = params.config.getLayout(layoutName);
        if (layout == null) {
            throw new RuntimeException("Unknown layout '" + layoutName + "'");
        }

        Document doc = new Document(layout.getFirstPageSize(null,params.jsonSpec));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        //PdfWriter writer = PdfWriter.getInstance(doc, params.outputStream);        
        PdfWriter writer = PdfWriter.getInstance(doc, bos);    
        
        if (!layout.isSupportLegacyReader()) {
            writer.setFullCompression();
            writer.setPdfVersion(PdfWriter.PDF_VERSION_1_5);
            writer.setCompressionLevel(PdfStream.BEST_COMPRESSION);
        }
        
        RenderingContext context = new RenderingContext(doc, writer, params.config, params.jsonSpec, params.configDir.getPath(), layout, params.headers);
        
        layout.render(params.jsonSpec, context);

        doc.close();
        writer.close();
        

//        ___   _        __  ___                            __            __              __ 
//        / _ \ (_)___ _ /  |/  /___ _ ___     ____ ___  ___/ /___    ___ / /_ ___ _ ____ / /_
//       / // // // _ `// /|_/ // _ `// _ \   / __// _ \/ _  // -_)  (_-</ __// _ `// __// __/
//      /____//_/ \_, //_/  /_/ \_,_// .__/   \__/ \___/\_,_/ \__/  /___/\__/ \_,_//_/   \__/ 
//               /___/              /_/                                                    
        
        
        Logger LOGGER1 = Logger.getLogger(hr.yottabyte.digmap.billing.CostCalc.class);
        LOGGER1.setLevel(Level.DEBUG);
        

		List<InputStream> attachmentList= new ArrayList<InputStream>();
		List<String> displayList = new ArrayList<String>();
		List<String> descriptionList = new ArrayList<String>();
		List<CostAndData> costAndDataList = new ArrayList<CostAndData>();
        DigMapConfig dc = new DigMapConfig(context.getConfig().getConfigDM());
        double cost = dc.getCost_per_excerpt();
        
        //take MFP server configuration and client specification to prepare WFS request 
		List<WfsLayersConfig> wlcl = WfsUtils.getLayersConfig(params.jsonSpec,context);
		for (WfsLayersConfig wlc : wlcl)
		{
			List<CostAndData> cadl = null;
			try {
				LOGGER.info("WFS base URL     : "+wlc.getBaseURL());
				LOGGER.info("WFS layers0      : "+wlc.getLayers()[0]);
				LOGGER.info("WFS filter       : "+wlc.getGeometry().toText());
				LOGGER.info("WFS filter srs   : "+wlc.getGeometry().getSRID());
				//LOGGER.info("WFS filter crs   : "+wlc.getCrs().toString());

				

				cadl = CostCalc.getDataAndCost(
						wlc.getBaseURL(),
						wlc.getLayers(),
						wlc.getGeometry(),
						dc.getCost_per_point(),
						dc.getCost_per_line(), 
						dc.getCost_per_polygon(),
						dc.getCost_per_area(), 0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (cadl!=null) costAndDataList.addAll(cadl);			
		}
		

		for (CostAndData costAndData : costAndDataList)
		{
		    cost += costAndData.getCost();
		    attachmentList.add(costAndData.getData());
		    displayList.add(costAndData.getLayer());
		    if (costAndData.getCost() > 0)
				descriptionList.add("Cost: " + costAndData.getCost());
			    else
				descriptionList.add("Data layer: "+costAndData.getLayer());
		}
		
		InputStream[] attachments = new InputStream[attachmentList.size()];
		attachments = attachmentList.toArray(attachments);
		
		String[] displays = new String[displayList.size()];
		displays = displayList.toArray(displays);
		
		String[] descriptions = new String[descriptionList.size()];
		descriptions = descriptionList.toArray(descriptions);	


		//take MapFishPrint document, add attachments and digital signature 
		ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
		
		try {
			//attach GML if there are some attachments
			if (!attachmentList.isEmpty()) {
				bos = PdfAttacher.addAttachments(bin, attachments, displays,descriptions);				
				LOGGER.info("GML data attached");
				//convert to input stream again
				bin = new ByteArrayInputStream(bos.toByteArray());
			};			
			
			//get DigMap signature configuration
			Properties properties = context.getConfig().getConfigDM();			
			bos = PdfSigner.signPdf(bin, properties);
			LOGGER.info("PDF signed");
			//write output back to MapFishPrint stream
			bos.writeTo(params.outputStream);
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		   ___   _        __  ___                            __                     __
//		   / _ \ (_)___ _ /  |/  /___ _ ___     ____ ___  ___/ /___    ___  ___  ___/ /
//		  / // // // _ `// /|_/ // _ `// _ \   / __// _ \/ _  // -_)  / -_)/ _ \/ _  / 
//		 /____//_/ \_, //_/  /_/ \_,_// .__/   \__/ \___/\_,_/ \__/   \__//_//_/\_,_/  
//		          /___/              /_/        http://patorjk.com/software/taag
		
        return context;
    }
}
