package cern.modesti.request.point;

import com.mysema.query.types.expr.BooleanExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Controller
public class PointController {

  @Autowired
  private PointRepository repository;

  @RequestMapping(value = "/points", method = GET, produces = "application/json")
  ResponseEntity<Resources<Point>> index(@RequestParam("search") String search) {

    PointPredicateBuilder builder = new PointPredicateBuilder();
    Pattern pattern = Pattern.compile("([\\w.]+?)(:|<|>)(\\w.+?),");
    Matcher matcher = pattern.matcher(search + ",");
    while (matcher.find()) {
      builder.with(matcher.group(1), matcher.group(2), matcher.group(3));
    }

    BooleanExpression predicate = builder.build();
    if (predicate == null) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }


    Iterable<Point> points = repository.findAll(predicate);


    Resources<Point> resources = new Resources<>(points);
    return new ResponseEntity<>(resources, HttpStatus.OK);
  }
}
