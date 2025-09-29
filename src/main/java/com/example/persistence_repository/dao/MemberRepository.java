package com.example.persistence_repository.dao;

import com.example.persistence_repository.entity.Member;
import com.example.persistence_repository.persistence.repository.AbstractRepository;

public class MemberRepository extends AbstractRepository<Member, Integer> {

    public MemberRepository() {
        super(Member.class);
    }

}
