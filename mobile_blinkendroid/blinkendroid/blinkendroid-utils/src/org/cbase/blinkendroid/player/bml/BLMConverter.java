package org.cbase.blinkendroid.player.bml;
/*
 * Copyright 2010 the original author or authors.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.xmlpull.mxp1.MXParser;

public class BLMConverter {

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
	// get all files in bml convert them to bbm
	File bmlDir = new File("gdd/blms");
	File[] bmls = bmlDir.listFiles();
	for (int i = 0; i < bmls.length; i++) {
		try{
	    if(!bmls[i].getName().endsWith(".blm"))
		continue;
	    System.out.println("convert " + bmls[i].getName());
	    String tname=bmls[i].getName().substring(0, bmls[i].getName().length()-4)/*.replace("-", "")*/;
	    tname=tname.toLowerCase();
	    BLM blm = convert("gdd/blms/" + bmls[i].getName(), "gdd/bbm2/"+tname+".bbm");
	    compress( "gdd/bbm2/"+tname+".bbm","gdd/bbmz2/" +tname+ ".bbmz");
	    //die infofiles f�r den server
	    ObjectOutput out = new ObjectOutputStream(new FileOutputStream("gdd/bbmz2/" +tname+ ".info"));
	    out.writeObject(blm.header);
	    out.flush();
	    out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
    }

    public static BLM convert(String blmfile, String bbmfile) throws IOException {
    	BMLParser p = new BMLParser();
	BLM blm = p.parseBLM(new FileReader(blmfile));
	if(null==blm.header.title)
	    blm.header.title="orig"+blmfile.substring(4,blmfile.length()-4);
	// write blm to binary blinkelights movie bbm
	ObjectOutput out = new ObjectOutputStream(new FileOutputStream(bbmfile));
	out.writeObject(blm);
	out.flush();
	out.close();
	return blm;
    }

    public static void compress(String filein, String fileout) {
	FileInputStream fis = null;
	FileOutputStream fos = null;
	try {
	    fis = new FileInputStream(filein);
	    fos = new FileOutputStream(fileout);
	    ZipOutputStream zos = new ZipOutputStream(fos);
	    ZipEntry ze = new ZipEntry(filein);
	    zos.putNextEntry(ze);
	    final int BUFSIZ = 4096;
	    byte inbuf[] = new byte[BUFSIZ];
	    int n;
	    while ((n = fis.read(inbuf)) != -1)
		zos.write(inbuf, 0, n);
	    fis.close();
	    fis = null;
	    zos.close();
	    fos = null;
	} catch (IOException e) {
	    System.err.println(e);
	} finally {
	    try {
		if (fis != null)
		    fis.close();
		if (fos != null)
		    fos.close();
	    } catch (IOException e) {
	    }
	}
    }
}