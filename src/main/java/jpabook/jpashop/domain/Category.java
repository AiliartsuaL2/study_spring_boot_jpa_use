package jpabook.jpashop.domain;

import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class  Category {
    @Id
    @GeneratedValue
    @Column(name="category_id")
    private Long id;

    private String name;

    @ManyToMany
    @JoinTable(name = "category_item",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id"))
    private List<Item> items = new ArrayList<>();

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="parent_id") // 같은 테이블 내 계층구조도 다른 엔티티 매핑하는것 처럼 똑같이 해줌.
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> child = new ArrayList<>();

    //== 연관관계 메서드==//
    public void addChildCategory(Category child){
        this.child.add(child);
        child.setParent(this);
    }
}
