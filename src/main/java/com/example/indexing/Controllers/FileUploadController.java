package com.example.indexing.Controllers;


import com.example.indexing.Services.FileServices;
import com.example.indexing.Services.storage.StorageFileNotFoundException;
import com.example.indexing.Services.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

@Controller
public class FileUploadController {

    @Autowired
    private final StorageService storageService;
    @Autowired
    private FileServices fileServices;

    @Autowired
    public FileUploadController(StorageService storageService, FileServices fileServices) {
        this.storageService = storageService;
        this.fileServices=fileServices;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model, @RequestParam(value = "search", required = false) String term) throws IOException {

        model.addAttribute("files", storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveFile", path.getFileName().toString()).build().toString())
                .collect(Collectors.toList()));
        model.addAttribute("term", term);

        model.addAttribute("index", MvcUriComponentsBuilder.fromMethodName(FileUploadController.class, "serveIndexFile").build().toString());


        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }


    @GetMapping("/indexes")
    @ResponseBody
    public ResponseEntity<Resource> serveIndexFile() {
        try {
            Path rootLocation = Paths.get("./index/indexes.txt");

            Resource resource = new UrlResource(rootLocation.toUri());

            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + resource.getFilename() + "\"").body(resource);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes, HttpServletRequest request, Model model) {
        storageService.store(file);
        fileServices.addFile();
        fileServices.writeToFile();

        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/";
    }


    @GetMapping("/search")
    public String searchRes(){
        return "searchForm";
    }
    @PostMapping("/search")
    public String search(Model model, HttpServletRequest request) throws IOException, SAXException, ParserConfigurationException {

        String query=request.getParameter("name");
        String[] terms=fileServices.tokenization(query);
        model.addAttribute("query", query);
        PriorityQueue<double[]> ranks=fileServices.getRanking(terms);

        model.addAttribute("ranks", fileServices.rankingTopK(20, ranks));
        return "resultForm";
    }




    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }




}
