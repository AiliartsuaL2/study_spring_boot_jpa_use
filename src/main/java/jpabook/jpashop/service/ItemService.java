package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional // 리드온리이면 save 처리가 안됨, 조회시에만 readonly 먹게
    public void saveItem(Item item){
        itemRepository.save(item);
    }

    @Transactional
    public void updateItem(Long itemId, int price, int stockQuantity, String name){
        Item findItem = itemRepository.findOne(itemId); // 영속 상태의 item을 꺼내옴
        findItem.setPrice(price);
        findItem.setStockQuantity(stockQuantity);
        findItem.setName(name); // 영속 상태이기 때문에 변경감지로 자동으로 update 쿼리를 날려준다!!
    }

    public List<Item> findItems(){
        return itemRepository.findAll();
    }
    public Item findOne(Long itemId){
        return itemRepository.findOne(itemId);
    }
}
