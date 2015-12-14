//PROCESS_ANNOTATIONS

protected ProcessingResult processAnnotations(RootDeploymentDescriptor bundleDesc,
                                              ModuleScanner scanner,
                                              ReadableArchive archive)
            throws AnnotationProcessorException, IOException {

        // in embedded mode, I ignore all scanners and parse all possible classes.
        if (archive instanceof ScatteredArchive) {
            return super.processAnnotations(bundleDesc, this.scanner, archive);
        } else {
            return super.processAnnotations(bundleDesc, scanner, archive);
        }
    }
	
	//READ_STANDARD_DEPLOYMENT_DESCRIPTOR
	
	public T/*controllare*/ readStandardDeploymentDescriptor(ReadableArchive archive)
             throws IOException, SAXParseException {
 
         InputStream is = null;
 
         try {
             is = archive.getEntry(getStandardDDFile().getDeploymentDescriptorPath());
             if (is != null) {
                 DeploymentDescriptorFile<T> ddFile = getStandardDDFile();
                 ddFile.setXMLValidation(getXMLValidation());
                 ddFile.setXMLValidationLevel(validationLevel);
                 if (archive.getURI() != null) {
                     ddFile.setErrorReportingString(archive.getURI().getSchemeSpecificPart());
                 }
                 T result = ddFile.read(is);
                 return result;
             } else {
                 /*
                  *Always return at least the default, because the info is needed 
                  *when an app is loaded during a server restart and there might not
                  *be a physical descriptor file.
                  */
                 return getDefaultBundleDescriptor();
             }
         } finally {
             if (is != null) {
                 is.close();
             }
         }
     }
	 
	 //WRITE
	 
	 public void write(ReadableArchive in, String outPath) throws IOException {
 
         ReadableArchive oldArchive = null;
         try {
             oldArchive = archiveFactory.openArchive(new File(outPath));
         } catch (IOException ioe) {
             // there could be many reasons why we cannot open this archive, 
             // we should continue
         }
         WritableArchive out = null;
         BufferedOutputStream bos = null;
         try {
             File outputFile=null;
             if (oldArchive != null && oldArchive.exists() &&
                     !(oldArchive instanceof WritableArchive)) {
                 // this is a rewrite, get a temp file name...
                 // I am creating a tmp file just to get a name
                 outputFile = getTempFile(outPath);
                 outputFile.delete();
                 out = archiveFactory.createArchive(outputFile);
                 oldArchive.close();
             } else {
                 out = archiveFactory.createArchive(new File(outPath));
             }
 
             // write archivist content
             writeContents(in, out);
             out.close();
             in.close();
 
             // if we were using a temp file, time to rewrite the original
             if (outputFile != null) {
                 ReadableArchive finalArchive = archiveFactory.openArchive(new File(outPath));
                 finalArchive.delete();
                 ReadableArchive tmpArchive = archiveFactory.openArchive(outputFile);
                 tmpArchive.renameTo(outPath);
             }
         } catch (IOException ioe) {
             // cleanup
             if (out != null) {
                 try {
                     out.close();
                     //out.delete(); <-- OutputJarArchive.delete isn't supported.
                 } catch (IOException outIoe) {
                     // ignore exceptions here, otherwise this will end up masking the real
                     // IOException in 'ioe'.
                 }
             }
             // propagate exception
             throw ioe;
         }
     }
	 
	 //WRITECONTENTS
	 
	 protected void writeContents(ReadableArchive in, WritableArchive out, Vector entriesToSkip)
            throws IOException {

        // prepare the manifest file to add the main class entry
        if (manifest == null) {
            manifest = new Manifest();
        }
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, MANIFEST_VERSION_VALUE);
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS,
                ((ApplicationClientDescriptor) getDescriptor()).getMainClassName());

        super.writeContents(in, out, entriesToSkip);
    }
