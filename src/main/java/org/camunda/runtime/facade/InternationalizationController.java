package org.camunda.runtime.facade;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.runtime.facade.dto.Language;
import org.camunda.runtime.jsonmodel.Translation;
import org.camunda.runtime.security.annotation.IsAdmin;
import org.camunda.runtime.security.annotation.IsAuthenticated;
import org.camunda.runtime.service.InternationalizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/api/i18n")
public class InternationalizationController {

  @Autowired private InternationalizationService intlService;

  @IsAdmin
  @PostMapping
  public ResponseEntity<Translation> save(@RequestBody Translation theme) throws IOException {
    intlService.saveTranslation(theme);
    return new ResponseEntity<>(theme, HttpStatus.CREATED);
  }

  @IsAdmin
  @GetMapping("/{languageName}")
  @ResponseBody
  public Translation getTranslation(@PathVariable String languageName) throws IOException {
    return intlService.findByName(languageName);
  }

  @IsAdmin
  @DeleteMapping("/{languageName}")
  public void delete(@PathVariable String languageName) throws IOException {
    intlService.deleteByName(languageName);
  }

  @IsAuthenticated
  @GetMapping(value = "/languages")
  @ResponseBody
  public List<Language> languages() {
    Collection<Translation> translations = intlService.all();
    List<Language> languages = new ArrayList<>();
    for (Translation translation : translations) {
      languages.add(new Language(translation.getCode(), translation.getName()));
    }
    return languages;
  }

  @GetMapping("/{ln}/{ns}.json")
  public Map<String, String> translation(@PathVariable String ln)
      throws StreamReadException, DatabindException, IOException {
    Translation t = intlService.findByCode(ln);
    if (t != null) {
      return t.getSiteTranslations();
    }
    return new HashMap<>();
  }
}
