/*
 * Copyright (c) 2010, Soar Technology, Inc.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * * Neither the name of Soar Technology, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without the specific prior written permission of Soar Technology, Inc.
 * 
 * THIS SOFTWARE IS PROVIDED BY SOAR TECHNOLOGY, INC. AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SOAR TECHNOLOGY, INC. OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Created on Mar 28, 2009
 */
package com.soartech.simjr.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author ray
 */
public class FileTools
{
    public static String getExtension(File file)
    {
        return getExtension(file.getName());
    }
    
    public static String getExtension(String file)
    {
        int dot = file.lastIndexOf('.');
        int slash = file.lastIndexOf('/');
        int backslash = file.lastIndexOf('\\');
        if(dot == -1)
        {
            return "";
        }
        
        if((slash != -1 && dot < slash) || (backslash != -1 && dot < backslash))
        {
            return "";
        }
        return file.substring(dot+1);
    }
    
    public static String getFilenameFromPath(String file)
    {
        int slash = file.lastIndexOf('/');
        int backslash = file.lastIndexOf('\\');
        
        if(slash != -1)
        {
            return file.substring(slash + 1);
        }
        else if(backslash != -1)
        {
            return file.substring(backslash + 1);
        }
        
        return "";
    }
    
    /**
     * If the file represents a directory, it is returned, otherwise, its 
     * parent directory is returned.
     * 
     * @param file the file
     * @return a file representing a directory
     */
    public static File asDirectory(File file)
    {
        return file.isDirectory() ? file : file.getParentFile();
    }
    
    /**
     * If there is no extension on the file, add the provided extension
     * 
     * @param file the file
     * @param extension the default extension to add
     * @return the resulting file
     */
    public static File addDefaultExtension(File file, String extension)
    {
        if(getExtension(file.getName()).length() == 0)
        {
            file = new File(file.getParent(), file.getName() + "." + extension);
        }
        return file;
    }
    
    /**
     * Copy an input stream to an output stream
     * 
     * @param from the input stream to copy from
     * @param to the output stream to copy to
     * @throws IOException
     */
    public static void copy(InputStream from, OutputStream to) throws IOException
    {
        final byte[] buffer = new byte[8092];
        int r = from.read(buffer);
        while(r != -1)
        {
            to.write(buffer, 0, r);
            r = from.read(buffer);
        }
    }
    
    /**
     * Copy one file to another
     * 
     * @param from the file to copy
     * @param to the file to create
     * @throws IOException
     */
    public static void copy(File from, File to) throws IOException
    {
        InputStream fromStream = null;
        OutputStream toStream = null;
        try
        {
            fromStream = new BufferedInputStream(new FileInputStream(from));
            toStream = new BufferedOutputStream(new FileOutputStream(from));
            copy(fromStream, toStream);
        }
        finally
        {
            try
            {
                if(fromStream != null)
                {
                    fromStream.close();
                }
            }
            finally
            {
                if(toStream != null)
                {
                    toStream.close();
                }
            }
        }
    }
    
    public static File copyToTempFile(InputStream from, String prefix, String suffix) throws IOException
    {
        final File tempFile = File.createTempFile(prefix, suffix);
        final OutputStream os = new BufferedOutputStream(new FileOutputStream(tempFile));
        try
        {
            copy(from, os);
            return tempFile;
        }
        finally
        {
            os.close();
        }
    }
    
    /**
     * Writes a resource to a temporary file so it can be read by some other system
     * such as Soar.
     * 
     * @param resource The resource path
     * @param prefix The temp file prefix
     * @param suffix The temp file suffix
     * @return The resulting file object
     * @throws IOException
     */
    public static File writeResourceToTempFile(ClassLoader loader, String resource, String prefix, String suffix) throws IOException
    {
        final InputStream is = loader.getResourceAsStream(resource);
        if(is == null)
        {
            throw new FileNotFoundException(resource);
        }
        
        try
        {
            final File temp = copyToTempFile(is, prefix, suffix);
            temp.deleteOnExit();
            return temp;
        }
        finally
        {
            is.close();
        }
    }

}
