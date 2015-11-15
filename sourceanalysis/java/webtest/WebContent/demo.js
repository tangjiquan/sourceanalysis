function getPDF(){
            
            window.requestFileSystem(LocalFileSystem.PERSISTENT,0,function(fs){
                                     fs.root.getDirectory("Downloads",{create:true,exclusive:false},function(dir){
                                                          dir.getFile("pdfFile.pdf",null,function(pdfFile){
      cordova.exec(function(){},function(){},"FoxitPDFPlugin","openPDF",[{
                                                                         FilePath: pdfFile.fullPath,
                                                                         AnnotationAuthor: "Firas Barakat"
                                                                         }]);
      },function(){
      loader.innerHTML = "Downloading...";
      document.getElementById("loader").style.visibility = "visible";
      
      var ft = new FileTransfer();
      ft.onProgress = function(progressEvent){
      setPercentage(progressEvent.loaded / progressEvent.total);
      };
      ft.download(
                  encodeURI(fileURL),
                  dir.fullPath + "/pdfFile.pdf",
                  function(fileEntry){
                  document.getElementById("loader").style.visibility = "hidden";
                  cordova.exec(function(){},function(){},"FoxitPDFPlugin","openPDF",[{
                                                                                     FilePath: fileEntry.fullPath,
                                                                                     AnnotationAuthor: "Firas Barakat"
                                                                                     }]);
                  },
                  function(error){
                  console.log("Download Error: " + JSON.stringify(error));
                  document.getElementById("loader").style.visibility = "hidden";
                  }
      );
                                                     },fileError);
                                     },fileError);
                                     });
        }