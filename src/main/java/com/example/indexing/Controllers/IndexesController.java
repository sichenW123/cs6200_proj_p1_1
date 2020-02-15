package com.example.indexing.Controllers;


import com.example.indexing.Services.FileServices;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * Created by Enzo Cotter on 2/11/20.
 */
@Controller
public class IndexesController {
    @RequestMapping(value = "/terms", method = RequestMethod.GET)
    public String getAllTerms(Model model) {
        FileServices.AllFiles();
        List<String> terms = FileServices.writeToFile(FileServices.termList);
//        List<String> terms=new ArrayList<>();
//        terms.add("a");
//        terms.add("b");

        model.addAttribute("terms", terms);
        return "indexForm";
    }

    @RequestMapping(value = "/terms/{term}", method = RequestMethod.GET)
    public String getAllTerms(Model model, @PathVariable("term") String term) {
        List<String> docs = FileServices.termMap.get(term);
        if (docs.isEmpty()) return "no result";
        model.addAttribute("term", term);
        model.addAttribute("docs", docs);
        return "singleTerm";
    }
}
