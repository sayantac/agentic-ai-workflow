package demo.ai.agentic.repository;

import demo.ai.agentic.record.Dog;
import org.springframework.data.repository.ListCrudRepository;

public interface DogRepository extends ListCrudRepository<Dog, Integer> {
} 