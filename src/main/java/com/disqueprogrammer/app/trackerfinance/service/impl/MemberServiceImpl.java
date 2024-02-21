package com.disqueprogrammer.app.trackerfinance.service.impl;

import com.disqueprogrammer.app.trackerfinance.persistence.entity.Member;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectExistsException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectNotFoundException;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.MemberRepository;
import com.disqueprogrammer.app.trackerfinance.persistence.repository.TransactionRepository;
import com.disqueprogrammer.app.trackerfinance.service.interfaz.IMemberService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MemberServiceImpl implements IMemberService {

    private final static Logger LOG = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final MemberRepository memberRepository;

    private final TransactionRepository transactionRepository;

    public MemberServiceImpl(MemberRepository memberRepository, TransactionRepository transactionRepository) {
        this.memberRepository = memberRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Member save(Member memberRequest) throws ObjectExistsException, ObjectNotFoundException {
        validNameMember(memberRequest.getName(), memberRequest.getUserId());
        if(!StringUtils.isEmpty(memberRequest.getEmail())) {
            validEmailMember(memberRequest.getEmail(), memberRequest.getUserId());
        }
        return memberRepository.save(memberRequest);
    }

    private void validNameMember(String name, Long userId) {
        if(StringUtils.isEmpty(name)) throw new IllegalArgumentException("El campo nombre es requerido");

        Member memberFoundedByName = memberRepository.findByNameAndUserId(name, userId);
        if (memberFoundedByName != null) throw new IllegalArgumentException("Ya existe un miembro con el nombre: " + name + ", ingrese otro");
    }

    private void validEmailMember(String email, Long userId) {
        boolean hasValidFormatEmail = validEmailFormat(email);
        if(!hasValidFormatEmail)
            throw new IllegalArgumentException("El email ingresado no tiene un formato correcto");

        Member memberFoundedByEmail = memberRepository.findByEmailAndUserId(email, userId);
        if (memberFoundedByEmail != null) throw new IllegalArgumentException("Ya existe un miembro con el email: " + email + ", ingrese otro");
    }

    public boolean validEmailFormat(String email) {
        Pattern pattern = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @Override
    public Member findByIdAndUserId(Long memberId, Long userId) throws ObjectNotFoundException {

        Member memberFounded = memberRepository.findByIdAndUserId(memberId, userId);
        if(memberFounded == null)
            throw  new ObjectNotFoundException("No se encontró al miembro seleccionado");

        return memberFounded;
    }

    @Override
    public List<Member> findByUserId(Long userId) {
        return memberRepository.findByUserId(userId);
    }

    @Override
    public Member update(Member memberRequest, Long memberId) throws ObjectExistsException, ObjectNotFoundException {

        Member memberFounded = memberRepository.findByIdAndUserId(memberId, memberRequest.getUserId());

        if(memberFounded == null)
            throw  new ObjectNotFoundException("No se encontró al miembro seleccionado");

        if(!memberRequest.getName().equals(memberFounded.getName())) {
            validNameMember(memberRequest.getName(), memberRequest.getUserId());
        }

        if(!memberFounded.getEmail().equals(memberRequest.getEmail())) {
            validEmailMember(memberRequest.getEmail(), memberRequest.getUserId());
        }

        memberFounded.setName(memberRequest.getName().toLowerCase());
        memberFounded.setEmail(memberRequest.getEmail());

        return memberRepository.save(memberFounded);
    }

    @Override
    public void delete(Long memberId, Long userId) throws ObjectNotFoundException {

        Member memberFounded = memberRepository.findByIdAndUserId(memberId, userId);
        if (memberFounded == null)
            throw new ObjectNotFoundException("No se encontró al usuario seleccionado");

        memberRepository.deleteById(memberId);
    }

    private void validateParamas(Member memberRequest) throws ObjectExistsException, ObjectNotFoundException {

        if(StringUtils.isEmpty(memberRequest.getName()) || memberRequest.getName() == null){
            throw new ObjectNotFoundException("El campo nombre no puede ser vacío o nulo");
        }

        if(StringUtils.isEmpty(memberRequest.getEmail()) || memberRequest.getEmail() == null){
            Member memberNameRepeated = memberRepository.findByEmailAndUserId(memberRequest.getEmail(), memberRequest.getUserId());
            if(memberNameRepeated != null) {
                throw new ObjectExistsException("Ya existe un miembro con el correo que intentas registrar");
            }
        }
    }

}
