package cern.modesti.request.point;

import cern.modesti.security.ldap.User;
import cern.modesti.security.ldap.UserRepository;
import com.mysema.query.types.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.querydsl.QuerydslPredicate;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Controller
public class PointController {

  @Autowired
  private PointRepository repository;

  @RequestMapping(value = "/points", method = RequestMethod.GET)
  ResponseEntity<Resources<Point>> index(@QuerydslPredicate(root = Point.class) Predicate predicate) {

    Iterable<Point> points = repository.findAll(predicate);

    

    Resources<Point> resources = new Resources<>(points);
    return new ResponseEntity<>(resources, HttpStatus.OK);
  }
}
