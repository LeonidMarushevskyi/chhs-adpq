package com.engagepoint.cws.apqd.web.rest;

import com.engagepoint.cws.apqd.Application;
import com.engagepoint.cws.apqd.domain.Message;
import com.engagepoint.cws.apqd.domain.User;
import com.engagepoint.cws.apqd.domain.enumeration.MessageStatus;
import com.engagepoint.cws.apqd.repository.DeletedRepository;
import com.engagepoint.cws.apqd.repository.DraftRepository;
import com.engagepoint.cws.apqd.repository.InboxRepository;
import com.engagepoint.cws.apqd.repository.MailBoxRepository;
import com.engagepoint.cws.apqd.repository.MessageRepository;
import com.engagepoint.cws.apqd.repository.OutboxRepository;
import com.engagepoint.cws.apqd.repository.UserRepository;
import com.engagepoint.cws.apqd.repository.search.MessageSearchRepository;
import com.engagepoint.cws.apqd.repository.search.MessageThreadSearchRepository;
import com.engagepoint.cws.apqd.web.websocket.MailBoxService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import static com.engagepoint.cws.apqd.APQDTestUtil.prepareMailBox;
import static com.engagepoint.cws.apqd.APQDTestUtil.prepareMessage;
import static com.engagepoint.cws.apqd.APQDTestUtil.prepareUser;
import static com.engagepoint.cws.apqd.APQDTestUtil.setCurrentUser;
import static com.engagepoint.cws.apqd.APQDTestUtil.setMailBox;
import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class MailResourceTest {
    private static final String MSG_SUBJECT = "subject";
    private static final String MSG_SUBJECT_UPDATED = "subject updated";
    private static final String MSG_BODY = "body";
    private static final String MSG_BODY_UPDATED = "body updated";
    private static final String CURRENT_LOGIN = "current1";
    private static final String TO_LOGIN = "userto1";

    enum MAIL_FILTER {
        NONE,
        LOGIN,
        BODY
    }

    @Inject
    private InboxRepository inboxRepository;

    @Inject
    private OutboxRepository outboxRepository;

    @Inject
    private DeletedRepository deletedRepository;

    @Inject
    private DraftRepository draftRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private PasswordEncoder passwordEncoder;

    @Inject
    private MessageRepository messageRepository;

    @Inject
    private MailBoxService mailBoxService;

    @Inject
    private MailBoxRepository mailBoxRepository;

    @Inject
    private MessageSearchRepository messageSearchRepository;

    @Inject
    private MessageThreadSearchRepository messageThreadSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restMailResourceMockMvc;

    private User fromUser;
    private User toUser;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        MailResource eMailResource = new MailResource();

        ReflectionTestUtils.setField(eMailResource, "userRepository", userRepository);
        ReflectionTestUtils.setField(eMailResource, "messageRepository", messageRepository);
        ReflectionTestUtils.setField(eMailResource, "mailBoxService", mailBoxService);
        ReflectionTestUtils.setField(eMailResource, "mailBoxRepository", mailBoxRepository);
        ReflectionTestUtils.setField(eMailResource, "messageSearchRepository", messageSearchRepository);
        ReflectionTestUtils.setField(eMailResource, "messageThreadSearchRepository", messageThreadSearchRepository);

        this.restMailResourceMockMvc = MockMvcBuilders.standaloneSetup(eMailResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void before() {
        messageRepository.deleteAll();
        messageSearchRepository.deleteAll();
        messageThreadSearchRepository.deleteAll();
    }

    private Message prepareData() {
        // create Users and Mailboxes

        fromUser = prepareUser(userRepository, passwordEncoder, CURRENT_LOGIN);
        setMailBox(userRepository, fromUser,
            prepareMailBox(mailBoxRepository, inboxRepository, outboxRepository, deletedRepository, draftRepository));

        setCurrentUser(fromUser);

        toUser = prepareUser(userRepository, passwordEncoder, TO_LOGIN);
        setMailBox(userRepository, toUser,
            prepareMailBox(mailBoxRepository, inboxRepository, outboxRepository, deletedRepository, draftRepository));

        // create new Message with no id

        return prepareMessage(null, MSG_SUBJECT, MSG_BODY, fromUser, toUser);
    }

    private void assertCreateMessage(Message newMessage) throws Exception {
        assertThat(newMessage.getId()).isNull();

        restMailResourceMockMvc.perform(
            put("/api/mails/draft")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(
                    newMessage
                )))
            .andExpect(status().isCreated());
    }

    private void assertGetMessages(Message testMessage, EMailDirectory eMailDirectory, MAIL_FILTER mailFilter) throws Exception {
        assertThat(testMessage.getId()).isNotNull();

        String searchWord = "-1";
        if (mailFilter == MAIL_FILTER.LOGIN) {
            searchWord = "BY_LOGIN_" + (eMailDirectory == EMailDirectory.INBOX
                ? testMessage.getFrom().getLogin() : testMessage.getTo().getLogin());
        } else if (mailFilter == MAIL_FILTER.BODY) {
            searchWord = testMessage.getBody().split(" ")[0];
        }

        restMailResourceMockMvc.perform(
            get(String.format("/api/mails/%s/%s?sort=id,desc", eMailDirectory, searchWord)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(testMessage.getId().intValue())))
            .andExpect(jsonPath("$.[*].subject").value(hasItem(testMessage.getSubject())))
            .andExpect(jsonPath("$.[*].body").value(hasItem(testMessage.getBody())));
    }

    private void assertUpdateMessage(Message updatedMessage) throws Exception {
        assertThat(updatedMessage.getId()).isNotNull();

        restMailResourceMockMvc.perform(
            put("/api/mails/draft")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(
                    updatedMessage
                )))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(updatedMessage.getId().intValue()))
            .andExpect(jsonPath("$.subject").value(updatedMessage.getSubject()))
            .andExpect(jsonPath("$.body").value(updatedMessage.getBody()));
    }

    private void assertSendMessage(Message message) throws Exception {
        assertThat(message.getId()).isNotNull();

        restMailResourceMockMvc.perform(
            post("/api/mails/draft")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(
                    message
                )))
            .andExpect(status().isOk());
    }

    private void assertConfirmReading(Message message) throws Exception {
        assertThat(message.getId()).isNotNull();

        restMailResourceMockMvc.perform(
            post("/api/mails/confirm")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(
                    message
                )))
            .andExpect(status().isOk());
    }

    private void assertMessageThread(Message message) throws Exception {
        assertThat(message.getId()).isNotNull();

        restMailResourceMockMvc.perform(
            get("/api/mails/thread/" + message.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.thread").exists())
            .andExpect(jsonPath("$.thread[*].id").value(hasItem(message.getId().intValue())))
            .andExpect(jsonPath("$.thread[*].subject").value(hasItem(message.getSubject())))
            .andExpect(jsonPath("$.thread[*].body").value(hasItem(message.getBody())));
    }

    @Test
    @Transactional
    public void testCreateGetUpdateSendRead() throws Exception {

        // test create

        assertCreateMessage(prepareData());

        Message testMessage = messageRepository.findAll().iterator().next();
        assertThat(testMessage.getStatus()).isEqualTo(MessageStatus.DRAFT);
        assertThat(testMessage.getDateCreated()).isNotNull();
        assertThat(testMessage.getDateUpdated()).isNull();
        assertThat(testMessage.getUnreadMessagesCount()).isEqualTo(0);

        // test get from fromUser drafts folder

        assertGetMessages(testMessage, EMailDirectory.DRAFTS, MAIL_FILTER.NONE);

        // test update

        testMessage.setSubject(MSG_SUBJECT_UPDATED);
        testMessage.setBody(MSG_BODY_UPDATED);

        assertUpdateMessage(testMessage);

        testMessage = messageRepository.findOne(testMessage.getId());
        assertThat(testMessage.getStatus()).isEqualTo(MessageStatus.DRAFT);
        assertThat(testMessage.getDateCreated()).isNotNull();
        assertThat(testMessage.getDateUpdated()).isNull();
        assertThat(testMessage.getUnreadMessagesCount()).isEqualTo(0);

        // test send

        assertSendMessage(testMessage);

        testMessage = messageRepository.findOne(testMessage.getId());
        assertThat(testMessage.getStatus()).isEqualTo(MessageStatus.UNREAD);
        assertThat(testMessage.getDateCreated()).isNotNull();
        assertThat(testMessage.getDateUpdated()).isNotNull();
        assertThat(testMessage.getUnreadMessagesCount()).isEqualTo(1);

        // test get from fromUser sent folder

        assertGetMessages(testMessage, EMailDirectory.SENT, MAIL_FILTER.NONE);
        assertGetMessages(testMessage, EMailDirectory.SENT, MAIL_FILTER.LOGIN);
        assertGetMessages(testMessage, EMailDirectory.SENT, MAIL_FILTER.BODY);

        testMessage = messageRepository.findOne(testMessage.getId());
        assertThat(testMessage.getStatus()).isEqualTo(MessageStatus.UNREAD);
        assertThat(testMessage.getUnreadMessagesCount()).isEqualTo(1);

        // test get from toUser inbox folder

        setCurrentUser(toUser);

        assertGetMessages(testMessage, EMailDirectory.INBOX, MAIL_FILTER.NONE);
        assertGetMessages(testMessage, EMailDirectory.INBOX, MAIL_FILTER.LOGIN);
        assertGetMessages(testMessage, EMailDirectory.INBOX, MAIL_FILTER.BODY);

        // test confirmReading

        assertConfirmReading(testMessage);

        testMessage = messageRepository.findOne(testMessage.getId());
        assertThat(testMessage.getStatus()).isEqualTo(MessageStatus.READ);

        // test message thread

        assertMessageThread(testMessage);
    }
}
