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

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@Controller
public class FileUploadController {

    private final StorageService storageService;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
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

    @GetMapping("/indexes/{term:.+}")
    public String getSingleIndex(Model model, @PathVariable("term") String term) {
        model.addAttribute("docs", FileServices.termMap.get(term));
        model.addAttribute("term", term);
        return "indexForm";

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
                                   RedirectAttributes redirectAttributes) {

        storageService.store(file);
        FileServices.AllFiles();
        FileServices.writeToFile(FileServices.termList);
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
